package com.cj.shop.service.impl;

import com.cj.shop.api.entity.GoodsVisit;
import com.cj.shop.api.entity.UserAddress;
import com.cj.shop.api.entity.UserCart;
import com.cj.shop.api.interf.UserApi;
import com.cj.shop.api.param.GoodsVisitRequest;
import com.cj.shop.api.param.UserCartRequest;
import com.cj.shop.api.param.select.StockSelect;
import com.cj.shop.api.response.PagedList;
import com.cj.shop.api.response.dto.*;
import com.cj.shop.common.utils.DateUtils;
import com.cj.shop.dao.mapper.GoodsMapper;
import com.cj.shop.dao.mapper.GoodsVisitMapper;
import com.cj.shop.dao.mapper.UserAddressMapper;
import com.cj.shop.dao.mapper.UserCartMapper;
import com.cj.shop.service.cfg.JedisCache;
import com.cj.shop.service.consts.ResultMsg;
import com.cj.shop.service.util.NumberUtil;
import com.cj.shop.service.util.PropertiesUtil;
import com.cj.shop.service.util.ResultMsgUtil;
import com.cj.shop.service.util.ValidatorUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuchuanWeng()
 * @date 2018/6/20
 * @since 1.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class UserService implements UserApi {
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private UserCartMapper userCartMapper;
    @Autowired
    private GoodsVisitMapper goodsVisitMapper;
    @Autowired
    private GoodsExtensionService goodsExtensionService;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private JedisCache jedisCache;
    public static final String JEDIS_PREFIX_USER = "cj_shop:mall:user:";
    public static final String JEDIS_PREFIX_CART = "cj_shop:mall:cart:";
    public static final String JEDIS_PREFIX_VISIT = "cj_shop:mall:visit:";

    /**
     * 用户添加收货地址
     *
     * @param userAddress
     * @return
     */
    @Override
    public String addAddress(UserAddress userAddress) {
        int i = userAddressMapper.insertSelective(userAddress);
        if (i > 0) {
            //添加成功 加入缓存
            Long id = userAddress.getId();
            String key = JEDIS_PREFIX_USER + "address:detail:" + id;
            jedisCache.setByDefaultTime(key, userAddress);
            jedisCache.hset(JEDIS_PREFIX_USER + "address:list:" + userAddress.getUid() + ":", id.toString(), userAddress);
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 用户查询全部收货地址
     *
     * @param uid
     */
    @Override
    public PagedList<UserAddress> getAllAddress(Long uid, Integer pageNum, Integer pageSize) {
        String key = JEDIS_PREFIX_USER + "address:list:" + uid + ":";
        long count = 0;
        Page<Object> objects = null;
        List<UserAddress> resultList = new ArrayList<>();
        if (pageNum != null && pageSize != null) {
            objects = PageHelper.startPage(pageNum, pageSize);
        } else {
            pageNum = 0;
            pageSize = 0;
        }
        //查询出ID列表
        List<Long> addrIds = ValidatorUtil.checkNotEmptyList(userAddressMapper.selectAllIds(uid));
        if (objects != null) count = objects.getTotal();
        //根据ID 去Redis中查询对象
        if (!addrIds.isEmpty()) {
            for (Long id : addrIds) {
                UserAddress userAddress = jedisCache.hget(key, id.toString(), UserAddress.class);
                if (userAddress == null) {
                    UserAddress detailById = getDetailById(uid, id);
                    jedisCache.hset(key, id.toString(), detailById);
                    resultList.add(detailById);
                } else {
                    resultList.add(userAddress);
                }
            }
        }
        PagedList<UserAddress> pagedList = new PagedList(resultList, count, pageNum, pageSize);
        return pagedList;
    }

    @Override
    public UserAddress getDetailById(Long uid, Long addr_id) {
        String key = JEDIS_PREFIX_USER + "address:detail:" + addr_id;
        UserAddress userAddress = jedisCache.get(key, UserAddress.class);
        if (userAddress == null) {
            userAddress = userAddressMapper.selectByPrimaryKey(uid, addr_id);
            jedisCache.setByDefaultTime(key, userAddress);
        }
        return userAddress;
    }

    /**
     * 用户删除收货地址
     *
     * @param addr_id
     */
    @Override
    public String deleteAddress(Long addr_id, Long uid) {
        UserAddress detailById = getDetailById(uid, addr_id);
        if (detailById == null) {
            return ResultMsg.ADDRESS_NOT_EXISTS;
        }

        int i = userAddressMapper.deleteByPrimaryKey(uid, addr_id);
        if (i > 0) {
            String key1 = JEDIS_PREFIX_USER + "address:detail:" + addr_id;
            String key2 = JEDIS_PREFIX_USER + "address:list:" + uid;
            jedisCache.del(key1);
            jedisCache.hdel(key2 + ":", addr_id.toString());
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 用户修改收货地址（包含设为默认）
     *
     * @param userAddress
     */
    @Override
    public String updateAddress(UserAddress userAddress, Map<String, Object> properties) {
        UserAddress detailById = getDetailById(userAddress.getUid(), userAddress.getId());
        if (detailById == null) {
            return ResultMsg.ADDRESS_NOT_EXISTS;
        }
        //
        if (1 == userAddress.getDefaultFlag()) {
            List<UserAddress> list = getAllAddress(userAddress.getUid(), null, null).getList();
            if (!list.isEmpty()) {
                for (UserAddress address : list) {
                    if (1 == address.getDefaultFlag()) {
                        return ResultMsg.DEFAULT_ADDR_ALREADY_EXIST;
                    }
                }
            }
        }
        userAddress.setProperties(PropertiesUtil.changeProperties(detailById.getProperties(), properties));
        int i = userAddressMapper.updateByPrimaryKeySelective(userAddress);
        if (i > 0) {
            String key1 = JEDIS_PREFIX_USER + "address:detail:" + userAddress.getId();
            String key2 = JEDIS_PREFIX_USER + "address:list:" + userAddress.getUid();
            jedisCache.del(key1);
            jedisCache.hdel(key2 + ":", userAddress.getId().toString());
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 加入购物车 执行逻辑：
     * 1、判断商品是否存在
     * 2、判断商品是否存在 以及规格是否相同 相同则数量+1 不同则加入一条记录
     * 3、保存加入时商品价格
     * 4、判断加入时的数量是否小于等于剩余库存 如是则允许加入
     *
     * @param request
     */
    @Override
    public String addCart(UserCartRequest request) {
        //查询商品是否存在
        StockSelect select = new StockSelect();
        select.setSGoodSn(request.getSGoodsSn());
        List<GoodsStockDto> list = goodsExtensionService.findAllGoodsStock(select).getList();
        //判断是否有这类商品
        if (list == null || list.isEmpty()) {
            return ResultMsg.GOOD_NOT_EXISTS;
        }
        GoodsStockDto stockDto = list.get(0);

        //判断库存剩余量
        Integer goodsNum = request.getGoodsNum();
        if (request.getGoodsNum() > stockDto.getStockNum()) {
            return ResultMsg.TOO_MANY_STOCKS;
        }
        UserCart userCart1 = userCartMapper.selectByUidGoodsId(request.getUid(), request.getSGoodsSn());
        int i;
        UserCart userCart = new UserCart();
        BeanUtils.copyProperties(request, userCart);
        userCart.setGoodsId(stockDto.getGoodsId());
        userCart.setProperties(PropertiesUtil.addProperties(request.getProperties()));
        if (userCart1 != null) {
            //直接增加相应数量
            i = userCartMapper.increGoodsNum(request.getUid(), request.getSGoodsSn(), goodsNum);
            if (i > 0) {
                UserCart cart = userCartMapper.selectByUidGoodsId(request.getUid(), request.getSGoodsSn());
                //delete cache
                jedisCache.hdel(JEDIS_PREFIX_CART, cart.getId().toString());
            }
        } else {
            //加入新商品
            i = userCartMapper.insertSelective(userCart);
            if (i > 0) {
                //delete cache
                jedisCache.hdel(JEDIS_PREFIX_CART, userCart.getId().toString());
            }
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 删除购物车商品
     *
     * @param cartId
     * @param uid
     */
    @Override
    public String deleteFromCart(Long cartId, Long uid) {
        int i = userCartMapper.deleteByPrimaryKey(cartId, uid);
        if (i > 0) {
            //add cache
            jedisCache.hdel(JEDIS_PREFIX_CART, cartId.toString());
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 修改购物车商品数量
     *
     * @return
     */
    @Override
    public String updateFromCart(UserCartRequest request) {
        Integer goodsNum = request.getGoodsNum();
        if (goodsNum != null && goodsNum <= 0)
            return ResultMsg.CART_NUMS_TOO_LESS;
        UserCartDto cartGoodById = getCartGoodById(request.getCartId(), request.getUid());
        GoodsStockDto stockById = goodsExtensionService.getStockById(cartGoodById.getStockId());
        if (cartGoodById == null || stockById == null)
            return ResultMsg.GOOD_NOT_EXISTS;
        //判断库存是否满足需求
        if (goodsNum != null && goodsNum > stockById.getStockNum())
            return ResultMsg.CART_NUMS_TOO_MUCH;
        //如果是修改规格 判断是否有同类型商品
        if (!StringUtils.isEmpty(request.getSGoodsSn())) {
            Map<String, List<UserCartDto>> collect = getGoodsFromCart(request.getUid(), null, null).getCollect();
            Set<Map.Entry<String, List<UserCartDto>>> entries = collect.entrySet();
            for (Map.Entry<String, List<UserCartDto>> enrty:entries) {
                List<UserCartDto> value = enrty.getValue();
                if (value != null && !value.isEmpty()) {
                    for (UserCartDto dto: value) {
                        if (request.getSGoodsSn().equals(dto.getSGoodsSn())) {
                            return ResultMsg.STOCK_ALREADY_EXISTS;
                        }
                    }
                }
            }
            // 如果不存在的话  则添加该类商品 数量默认为1
            request.setGoodsNum(1);
            return addCart(request);
        }
        //否则则是修改数量
        UserCart userCart = new UserCart();
        userCart.setUid(request.getUid());
        userCart.setId(request.getCartId());
        userCart.setGoodsNum(goodsNum);
        int i = userCartMapper.updateByPrimaryKeySelective(userCart);
        if (i > 0) {
            //add cache
            jedisCache.hdel(JEDIS_PREFIX_CART, request.getCartId().toString());
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 查询我的购物车
     *
     * @param uid
     */
    @Override
    public UserCartListDto getGoodsFromCart(Long uid, Integer page_num, Integer page_size) {
        Page<Object> objects = null;
        if (page_num != null && page_size != null) {
            objects = PageHelper.startPage(page_num, page_size);
        } else {
            page_num = 0;
            page_size = 0;
        }
        List<Long> longs = ValidatorUtil.checkNotEmptyList(userCartMapper.selectUserCartThings(uid));
        UserCartListDto userCartListDto = new UserCartListDto(objects == null ? longs.size() : objects.getTotal(), page_num, page_size);
        if (!longs.isEmpty()) {
            List<UserCartDto> list = new ArrayList<>();
            for (Long id : longs) {
                UserCartDto cartGoodById = getCartGoodById(id, uid);
                list.add(cartGoodById);
            }
            Map<String, List<UserCartDto>> collect = list.stream().collect(
                    Collectors.groupingBy(UserCartDto::getSupplyName, LinkedHashMap::new, Collectors.toList()));
            userCartListDto.setCount(longs.size());
            userCartListDto.setCollect(collect);
            userCartListDto.setSupplyArray(collect == null ? new LinkedHashSet<>() : collect.keySet());
        }
        return userCartListDto;
    }

    /**
     * 查询单条购物车详情
     *
     * @param cartId
     * @param uid
     */
    @Override
    public UserCartDto getCartGoodById(Long cartId, Long uid) {
        UserCartDto hget = jedisCache.hget(JEDIS_PREFIX_CART, cartId.toString(), UserCartDto.class);
        if (hget == null) {
            hget = userCartMapper.selectByPrimaryKey(cartId, uid);
            if (hget == null) {
                return new UserCartDto();
            }
            GoodsDto detail = goodsService.getGoodsDetail(hget.getGoodsId());
            StockSelect request = new StockSelect();
            request.setSGoodSn(hget.getSGoodsSn());
            request.setType("exist");
            //小编号查询指定库存商品
            List<GoodsStockDto> stockDtos = goodsExtensionService.findAllGoodsStock(request).getList();
            if (stockDtos != null && !stockDtos.isEmpty()) {
                GoodsStockDto goodsStockDto = stockDtos.get(0);
                hget.setSaleFlag(goodsStockDto.getSaleFlag());
                hget.setGoodsPrice(goodsStockDto.getSellPrice());
                hget.setSpecList(goodsStockDto.getSpecList());
            }
            //封装每一项的总价
            hget.setItemTotalPrice(Double.parseDouble(NumberUtil.DECIMAL_FORMAT.format(hget.getGoodsNum() * hget.getGoodsPrice())));
            hget.setGoodsId(detail.getId());
            hget.setBrandName(detail.getBrandName());
            hget.setSupplyName(detail.getSupplyName());
            hget.setGoodsName(detail.getGoodsName());
            hget.setGoodsImg(detail.getPreviewImg());
            hget.setShopName("珑讯自营");
            jedisCache.hset(JEDIS_PREFIX_CART, cartId.toString(), hget);
        }
        return hget;
    }

    /**
     * 添加访客记录
     *
     * @param request
     */
    @Override
    public String insertGoodsVisit(GoodsVisitRequest request) {
        GoodsDto dto = goodsMapper.selectByPrimaryKey(request.getGoodsId());
        if (dto == null) {
            return ResultMsg.GOOD_NOT_EXISTS;
        }
        GoodsVisit visit = goodsVisitMapper.selectVisitByUidGoodsId(request.getUid(), request.getGoodsId());
        int i = 0;
        long id;
        if (visit == null) {
            GoodsVisit goodsVisit = new GoodsVisit();
            BeanUtils.copyProperties(request, goodsVisit);
            //新增
            goodsVisit.setVisitTime(DateUtils.getCommonString());
            i = goodsVisitMapper.insertSelective(goodsVisit);
            id = goodsVisit.getId();
        } else {
            //修改
            visit.setVisitTime(DateUtils.getCommonString());
            i = goodsVisitMapper.updateByPrimaryKeySelective(visit);
            id = visit.getId();
        }
        if (i > 0) {
            //add cache
            jedisCache.hdel(JEDIS_PREFIX_VISIT, String.valueOf(id));
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 查询商品访问记录列表
     * 时间倒序
     *
     * @param uid
     * @param pageNum
     * @param pageSize
     */
    @Override
    public PagedList<GoodsVisitDto> findAllVisit(Long uid, Integer pageNum, Integer pageSize) {
        Page<Object> objects = null;
        List<GoodsVisitDto> list = new ArrayList<>();
        if (pageNum != null && pageSize != null) {
            objects = PageHelper.startPage(pageNum, pageSize);
        } else {
            pageNum = 0;
            pageSize = 0;
        }
        List<Long> longs = ValidatorUtil.checkNotEmptyList(goodsVisitMapper.getVisitIds(uid));
        if (!longs.isEmpty()) {
            for (Long id : longs) {
                GoodsVisitDto dto = getVisitDetail(id);
                list.add(dto);
            }
        }
        PagedList<GoodsVisitDto> pagedList = new PagedList<>(list, objects == null ? 0 : objects.getTotal(), pageNum, pageSize);
        return pagedList;
    }

    /**
     * 清除商品访问记录
     * type = all 清除全部
     * 否则根据visitId清除单条
     *
     * @param type
     * @param uid
     * @param visitId
     */
    @Override
    public String deleteVisit(String type, Long uid, Long visitId) {
        int i = 0;
        if ("all".equals(type)) {
            //删除全部浏览记录
            List<Long> ids = ValidatorUtil.checkNotEmptyList(goodsVisitMapper.getVisitIds(uid));
            i = goodsVisitMapper.deleteByUid(uid);
            if (!ids.isEmpty()) {
                for (Long id : ids) {
                    jedisCache.hdel(JEDIS_PREFIX_VISIT, id.toString());
                }
            }
        } else {
            i = goodsVisitMapper.deleteByPrimaryKey(visitId);
            jedisCache.hdel(JEDIS_PREFIX_VISIT, visitId.toString());
        }
        return ResultMsgUtil.dmlResult(i);
    }


    public GoodsVisitDto getVisitDetail(Long visitId) {
        GoodsVisitDto hget = jedisCache.hget(JEDIS_PREFIX_VISIT, visitId.toString(), GoodsVisitDto.class);
        if (hget == null) {
            hget = goodsVisitMapper.selectByPrimaryKey(visitId);
            jedisCache.hset(JEDIS_PREFIX_VISIT, visitId.toString(), hget);
        }
        return hget;
    }
}
