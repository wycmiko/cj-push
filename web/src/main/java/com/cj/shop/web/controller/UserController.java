package com.cj.shop.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cj.shop.api.entity.UserAddress;
import com.cj.shop.api.param.CartDeleteRequest;
import com.cj.shop.api.param.GoodsVisitRequest;
import com.cj.shop.api.param.UserAddressRequest;
import com.cj.shop.api.param.UserCartRequest;
import com.cj.shop.api.response.PagedList;
import com.cj.shop.api.response.dto.GoodsVisitDto;
import com.cj.shop.api.response.dto.UserCartDto;
import com.cj.shop.service.impl.GoodsService;
import com.cj.shop.service.impl.UserService;
import com.cj.shop.web.consts.ResultConsts;
import com.cj.shop.web.dto.Result;
import com.cj.shop.web.utils.ResultUtil;
import com.cj.shop.web.validator.CommandValidator;
import com.cj.shop.web.validator.TokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yuchuanWeng(wycmiko @ foxmail.com)
 * @date 2018/6/14
 * @since 1.0
 */
@RestController
@Slf4j
@RequestMapping("/v1/mall/json/user")
public class UserController {
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private GoodsService goodsService;

    /**
     * 查询地址详情
     *
     * @param id
     * @return
     */
    @GetMapping("/address/{id}")
    public Result getAddressDetail(@PathVariable Long id, String token) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(id, token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("getAddressDetail 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            UserAddress detail = userService.getDetailById(tokenValidator.getUidByToken(token), id);
            result.setData(detail == null ? new JSONObject() : detail);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getAddressDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询收货地址列表
     *
     * @param token
     * @return
     */
    @GetMapping("/addressList")
    public Result addressList(String token, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("addressList 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(userService.getAllAddress(uid, page_num, page_size));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addressList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 添加收货地址
     *
     * @param request
     * @return
     */
    @PostMapping("/addAddress")
    public Result addAddress(@RequestBody UserAddressRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getToken(), request.getUserName(), request.getMobile(), request.getDetailAddr())
                    || !CommandValidator.isMobile(request.getMobile())) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(request.getToken())) {
                log.info("addAddress 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(request.getToken());
            UserAddress address = new UserAddress();
            BeanUtils.copyProperties(request, address);
            address.setUid(uid);
            if (!StringUtils.isEmpty(request.getProperties())) {
                address.setProperties(JSON.toJSONString(request.getProperties()));
            } else {
                address.setProperties("{}");
            }
            String s = userService.addAddress(address);
            result = ResultUtil.getVaildResult(s, result);
            log.info("addAddress end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addAddress error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改收货地址(设默认)
     *
     * @param request
     * @return
     */
    @PutMapping("/updateAddr")
    public Result updateAddr(@RequestBody UserAddressRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getId(), request.getToken())
                    || !CommandValidator.isMobile(request.getMobile())) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(request.getToken())) {
                log.info("updateAddr 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(request.getToken());
            UserAddress address = new UserAddress();
            BeanUtils.copyProperties(request, address);
            address.setUid(uid);
            String s = userService.updateAddress(address, request.getProperties());
            result = ResultUtil.getVaildResult(s, result);
            log.info("updateAddr end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateAddr error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 删除收货地址(设默认)
     *
     * @param token
     * @return
     */
    @DeleteMapping("/deleteAddr/{id}")
    public Result deleteAddr(@PathVariable Long id, String token) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(id, token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("deleteAddr 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            String s = userService.deleteAddress(id, uid);
            result = ResultUtil.getVaildResult(s, result);
            log.info("deleteAddr end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deleteAddr error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 删除浏览记录
     *
     * @param token
     * @return
     */
    @DeleteMapping("/deleteVisit")
    public Result deleteVisit(String token, String type, Long visit_id) {
        //token校验
        Result result = null;
        try {
            log.info("deleteVisit begin token={},type={},visitId={}", token, type, visit_id);
            if (CommandValidator.isEmpty(type, token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!"all".equals(type)) {
                if (CommandValidator.isEmpty(visit_id)) {
                    return CommandValidator.paramEmptyResult();
                }
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("deleteVisit 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            String s = userService.deleteVisit(type, uid, visit_id);
            result = ResultUtil.getVaildResult(s, result);
            log.info("deleteVisit end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deleteVisit error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 修改、新增 访客记录
     *
     * @param request
     * @return
     */
    @PostMapping("/updateVisit")
    public Result updateVisit(@RequestBody GoodsVisitRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getToken(), request.getGoodsId())) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(request.getToken())) {
                log.info("updateVisit 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(request.getToken());
            request.setUid(uid);
            String s = userService.insertGoodsVisit(request);
            result = ResultUtil.getVaildResult(s, result);
            log.info("updateVisit end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateAddr error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询全部访客记录
     *
     * @param token
     * @return
     */
    @GetMapping("/visitList")
    public Result visitList(String token, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("visitList 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            PagedList<GoodsVisitDto> visit = userService.findAllVisit(uid, page_num, page_size);
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(visit);
            log.info("visitList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateAddr error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 加入/修改 购物车商品
     *
     * @param request
     * @return
     */
    @PostMapping("/addCart")
    public Result addCart(@RequestBody UserCartRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getToken(), request.getGoodsNum(), request.getSGoodsSn())) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(request.getToken())) {
                log.info("addCart 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(request.getToken());
            request.setUid(uid);
            String s = userService.addCart(request);
            result = ResultUtil.getVaildResult(s, result);
            log.info("addCart end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addCart error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 删除购物车商品
     *
     * @return
     */
    @DeleteMapping("/deleteCartItem")
    public Result deleteCartItem(@RequestBody CartDeleteRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getToken(), request.getCartList()) || request.getCartList().isEmpty()) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(request.getToken())) {
                log.info("deleteCartItem 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(request.getToken());
            List<Long> cartList = request.getCartList();
            String s = "";
            for (Long id : cartList) {
                s = userService.deleteFromCart(id, uid);
            }
            result = ResultUtil.getVaildResult(s, result);
            log.info("deleteCartItem end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deleteCartItem error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询全部购物车商品
     *
     * @return
     */
    @GetMapping("/getCartGoods")
    public Result getCartGoods(String token, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(token)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("getCartGoods 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            PagedList<UserCartDto> goodsFromCart = userService.getGoodsFromCart(uid, page_num, page_size);
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsFromCart);
            log.info("getCartGoods end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getCartGoods error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }
}
