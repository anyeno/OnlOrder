package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.controller.user.OrdersController;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.DistanceCalculator;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    @Transactional()
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 查询地址表
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 查询用户表
        User user = userMapper.selectById(BaseContext.getCurrentId());
        if (user == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(System.currentTimeMillis()+"");
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserName(user.getName());

        ordersMapper.insert(orders);
        log.info("订单id{}", orders.getId());

        // 构造订单明细数据 存入order_detail
        List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByUserId(BaseContext.getCurrentId());
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        shoppingCartList.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail, "id");
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetails);

        //清空购物车
        shoppingCartMapper.deleteByUderId(BaseContext.getCurrentId());

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }


    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.selectById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));


        // 直接支付成功 修改订单状态
        paySuccess(ordersPaymentDTO.getOrderNumber());

        // 根据订单号查询当前用户的订单
        Orders orders = ordersMapper.getByNumberAndUserId(
                ordersPaymentDTO.getOrderNumber(), BaseContext.getCurrentId());
        // 向商家发送来单提醒
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号: "+ordersPaymentDTO.getOrderNumber());

        webSocketServer.sendToAllClient(JSON.toJSONString(map));

        OrderPaymentVO vo = new OrderPaymentVO();
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    public OrderVO orderDetail(Long id) {
        Orders order = ordersMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> page = ordersMapper.getByUserIdAndStatus(BaseContext.getCurrentId(), dto.getStatus());
        List<OrderVO> records = new ArrayList<>();
//        List<Orders> orders = page.getResult();
//        for (int i = 0; i < orders.size(); i++) {
//            OrderVO orderVO = new OrderVO();
//            BeanUtils.copyProperties(orders.get(i), orderVO);
//            List<OrderDetail> orderDetails = orderDetailMapper.getById(orders.get(i).getId());
//            orderVO.setOrderDetailList(orderDetails);
//            records.add(orderVO);
//        }
        for(Orders orders: page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            List<OrderDetail> orderDetails = orderDetailMapper.getById(orders.getId());
            orderVO.setOrderDetailList(orderDetails);
            records.add(orderVO);
        }
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public void cancelOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());

        // 退款逻辑 这里直接设置为已退款
        orders.setPayStatus(Orders.REFUND);
        ordersMapper.update(orders);
    }


    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional()
    public void repeatOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        // 将订单中的商品加入到购物车
        // 判断该商品是否已经存在购物车 dishId+dishFlavor+userId
        // 补充缺失的属性 加入购物车表

        List<OrderDetail> orderDetails = orderDetailMapper.getById(id);
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
//        BeanUtils.copyProperties(orders, shoppingCart);
            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCart.setDishId(orderDetail.getDishId());
            shoppingCart.setSetmealId(orderDetail.getSetmealId());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            ShoppingCart select_cart = shoppingCartMapper.selectBy(shoppingCart);
            if (select_cart == null) { // 购物车没有该商品
                // 如果添加的是菜品
                if(shoppingCart.getDishId() != null) {
                    Dish dish = dishMapper.getById(shoppingCart.getDishId());
                    shoppingCart.setName(dish.getName());
                    shoppingCart.setAmount(dish.getPrice());
                    shoppingCart.setImage(dish.getImage());
                }else { // 添加的是套餐
                    Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                    shoppingCart.setName(setmeal.getName());
                    shoppingCart.setAmount(setmeal.getPrice());
                    shoppingCart.setImage(setmeal.getImage());
                }
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setNumber(1);
                // 存入购物车表中
                shoppingCartMapper.insert(shoppingCart);
            }else { // 购物车已有该商品
                select_cart.setNumber(select_cart.getNumber() + 1);
                shoppingCartMapper.update(select_cart);
            }
        }

    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer confirmedCount = ordersMapper.selectCountByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = ordersMapper.selectCountByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = ordersMapper.selectCountByStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setConfirmed(confirmedCount);
        vo.setDeliveryInProgress(deliveryInProgress);
        vo.setToBeConfirmed(toBeConfirmed);
        return vo;
    }

    /**
     * 管理端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> records = new ArrayList<>();
        for(Orders orders: page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            List<OrderDetail> orderDetails = orderDetailMapper.getById(orders.getId());
            orderVO.setOrderDetailList(orderDetails);
            records.add(orderVO);
        }
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public void adminCancelOrder(OrdersCancelDTO dto) {
        Orders orders = ordersMapper.getById(dto.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(dto.getCancelReason());

        // 退款逻辑 这里直接设置为已退款
        orders.setPayStatus(Orders.REFUND);
        ordersMapper.update(orders);
    }

    @Override
    public void completeOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void rejectionOrder(OrdersRejectionDTO dto) {
        Orders orders = ordersMapper.getById(dto.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(dto.getRejectionReason());

        // 退款逻辑 这里直接设置为已退款
        orders.setPayStatus(Orders.REFUND);
        ordersMapper.update(orders);
    }

    @Override
    public void confirmOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        orders.setStatus(Orders.CONFIRMED);

        ordersMapper.update(orders);
    }

    @Override
    public void deliveryOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        ordersMapper.update(orders);
    }


    @Autowired
    private ShopProperties shopProperties;

    @Override
    public Boolean isAccessAble(OrdersSubmitDTO ordersSubmitDTO) {
        // 计算商家的经纬度
        String url = new String("https://api.map.baidu.com/geocoding/v3");
        Map<String, String> params = new HashMap<String, String>();
        params.put("address", shopProperties.getAddress());
        params.put("ak", shopProperties.getAk());
        params.put("output", "json");
        String shop_address = HttpClientUtil.doGet(url, params);
        log.info("百度地图返回结果：{}", shop_address);
        JSONObject root = JSON.parseObject(shop_address);
        JSONObject result = root.getJSONObject("result");
        JSONObject location = result.getJSONObject("location");
        BigDecimal longitude = (BigDecimal) location.get("lng");
        BigDecimal latitude = (BigDecimal) location.get("lat");
        log.info("计算商户经纬值：{}, {}", longitude, latitude);

        // 计算用户的经纬度
        // 先查地址表
        // 查询地址表
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        String userAddress = addressBook.getProvinceName()+addressBook.getCityName()
                +addressBook.getDistrictName()+addressBook.getDetail();
        Map<String, String> paramsUser = new HashMap<String, String>();
        paramsUser.put("address", userAddress);
        paramsUser.put("ak", shopProperties.getAk());
        paramsUser.put("output", "json");

        String user_address = HttpClientUtil.doGet(url, paramsUser);
        log.info("百度地图返回结果：{}", user_address);
        JSONObject root_user = JSON.parseObject(user_address);
        JSONObject result_user = root_user.getJSONObject("result");
        JSONObject location_user = result_user.getJSONObject("location");
        BigDecimal longitude_user = (BigDecimal) location_user.get("lng");
        BigDecimal latitude_user = (BigDecimal) location_user.get("lat");
        log.info("计算用户经纬值：{}, {}", longitude, latitude);

        Double distance = DistanceCalculator.calculateDistance(latitude.doubleValue(), longitude.doubleValue(), latitude_user.doubleValue(), longitude_user.doubleValue());
        log.info("distance == {}", distance);
        if(distance <= 5000){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2 ); // 2表示催单
        map.put("orderId", id);
        map.put("content", "订单号: "+ orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 查询时间段的营业额
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        while(!startDate.isAfter(endDate)){
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end", endTime);
            Double turnover = ordersMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //数据封装
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

}
