package com.cj.shop.web.controller.manage;

import com.cj.shop.api.entity.ExpressCash;
import com.cj.shop.api.entity.GoodsSupply;
import com.cj.shop.api.param.*;
import com.cj.shop.api.param.select.GoodsSelect;
import com.cj.shop.api.param.select.StockSelect;
import com.cj.shop.api.response.PagedList;
import com.cj.shop.api.response.dto.GoodsDto;
import com.cj.shop.common.utils.DateUtils;
import com.cj.shop.service.impl.AsyncService;
import com.cj.shop.service.impl.GoodsExtensionService;
import com.cj.shop.service.impl.GoodsService;
import com.cj.shop.web.consts.ResultConsts;
import com.cj.shop.web.dto.Result;
import com.cj.shop.web.utils.ResultUtil;
import com.cj.shop.web.validator.CommandValidator;
import com.cj.shop.web.validator.TokenValidator;
import com.github.crab2died.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author yuchuanWeng(wycmiko @ foxmail.com)
 * @date 2018/6/25
 * @since 1.0
 */
@RestController
@Slf4j
@RequestMapping({"/v1/mall/manage/goods/", "/v1/mall/json/goods/"})
public class GoodsManageController {
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private GoodsExtensionService goodsExtensionService;

    /**
     * 查询供应商详情
     *
     * @param id
     * @return
     */
    @GetMapping("/supply/{id}")
    public Result getGoodsSupplyDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("getGoodsSupplyDetail begin");
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            GoodsSupply supplyDetail = goodsService.getSupplyDetail(id);
            result.setData(supplyDetail);
            log.info("getGoodsSupplyDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getGoodsSupplyDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询供应商列表
     *
     * @return
     */
    @GetMapping("/supplyList")
    public Result getGoodsSupplyList(Integer page_num, Integer page_size, String supply_name, String type) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(type)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!StringUtils.isBlank(supply_name)) {
                supply_name = URLDecoder.decode(supply_name, "UTF-8");
            }
            log.info("supplyList begin supplyName={}", supply_name);
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            PagedList<GoodsSupply> allSupplies = goodsService.findAllSupplies(supply_name, page_num, page_size, type);
            result.setData(allSupplies);
            log.info("supplyList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("supplyList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 添加供应商
     *
     * @return
     */
    @PostMapping("/addSupply")
    public Result addSupply(@RequestBody GoodsSupplyRequest goodsSupply) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(goodsSupply.getSupplyName())) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("addSupply begin");
            String s = goodsService.insertSupply(goodsSupply);
            log.info("addSupply end");
            result = ResultUtil.getVaildResult(s, result);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addSupply error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改供应商
     *
     * @return
     */
    @PutMapping("/updateSupply")
    public Result updateSupply(@RequestBody GoodsSupplyRequest goodsSupply) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(goodsSupply.getId(), goodsSupply.getSupplyName())) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("updateSupply begin");
            String s = goodsService.updateSupply(goodsSupply);
            result = ResultUtil.getVaildResult(s, result);
            log.info("updateSupply end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateSupply error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询品牌详情
     *
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    public Result getGoodsBrandDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("getGoodsBrandDetail begin");
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsService.getBrandDetail(id));
            log.info("getGoodsBrandDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getGoodsBrandDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询品牌列表
     *
     * @return
     */
    @GetMapping("/brandList")
    public Result brandList(Integer page_num, Integer page_size, String brand_name, String type) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(type)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!StringUtils.isBlank(brand_name)) {
                brand_name = URLDecoder.decode(brand_name, "UTF-8");
            }
            log.info("brandList begin");
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsService.findAllBrands(brand_name, page_num, page_size, type));
            log.info("brandList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("brandList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 添加品牌
     *
     * @return
     */
    @PostMapping("/addBrand")
    public Result addBrand(@RequestBody GoodsBrandRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getBrandName())) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("addBrand begin");
            result = ResultUtil.getVaildResult(goodsService.insertBrand(request), result);
            log.info("addBrand end");
        } catch (Exception e) {
            log.error("addBrand error {}", e.getMessage());
            e.printStackTrace();
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改品牌
     *
     * @return
     */
    @PutMapping("/updateBrand")
    public Result updateBrand(@RequestBody GoodsBrandRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getId(), request.getBrandName())) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("updateBrand begin");
            result = ResultUtil.getVaildResult(goodsService.updateBrand(request), result);
            log.info("updateBrand end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateBrand error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 添加商品规格
     *
     * @return
     */
    @PostMapping("/addSpec")
    public Result addSpec(@RequestBody GoodsSpecRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("addSpec begin");
            if (CommandValidator.isEmpty(request.getSpecName())) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.insertGoodsSpec(request));
            log.info("addSpec end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addSpec error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改商品规格
     *
     * @return
     */
    @PutMapping("/updateSpec")
    public Result updateSpec(@RequestBody GoodsSpecRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("updateSpec begin");
            if (CommandValidator.isEmpty(request.getId())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.updateGoodsSpec(request), result);
            log.info("updateSpec end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateSpec error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询商品规格详情
     *
     * @return
     */
    @GetMapping("/spec/{id}")
    public Result getSpecDetail(@PathVariable Long id, String type) {
        //token校验
        Result result = null;
        try {
            log.info("getSpecDetail begin");
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.getGoodsSpecDetail(id, type));
            log.info("getSpecDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getSpecDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询商品规格列表
     *
     * @return
     */
    @GetMapping("/specList")
    public Result specList(String type, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            log.info("specList begin");
            if (CommandValidator.isEmpty(type)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.findAllSpecs(page_num, page_size, type));
            log.info("specList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("specList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询商品标签列表
     *
     * @return
     */
    @GetMapping("/tagList")
    public Result tagList(String type, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            log.info("tagList begin");
            if (CommandValidator.isEmpty(type)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.findAllTags(page_num, page_size, type));
            log.info("tagList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("tagList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 查询商品标签详情
     *
     * @return
     */
    @GetMapping("/tag/{id}")
    public Result tagDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            log.info("tagDetail begin");
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.getGoodsTagDetail(id));
            log.info("tagDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("tagDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 添加商品标签
     *
     * @return
     */
    @PostMapping("/addTag")
    public Result addTag(@RequestBody GoodsTagRequest request) {
        //token校验
        Result result = null;
        try {
            if (CommandValidator.isEmpty(request.getTagName())) {
                return CommandValidator.paramEmptyResult();
            }
            log.info("addTag begin");
            result = ResultUtil.getVaildResult(goodsExtensionService.insertGoodsTag(request), result);
            log.info("addTag end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addTag error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改商品标签
     *
     * @return
     */
    @PutMapping("/updateTag")
    public Result updateTag(@RequestBody GoodsTagRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("updateTag begin");
            if (CommandValidator.isEmpty(request.getTagName())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.insertGoodsTag(request), result);
            log.info("updateTag end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateTag error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 添加单位
     *
     * @return
     */
    @PostMapping("/addUnit")
    public Result addUnit(@RequestBody GoodsUnitRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("addUnit begin");
            if (CommandValidator.isEmpty(request.getUnitName())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.insertGoodsUnit(request), result);
            log.info("addUnit end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addUnit error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改单位
     *
     * @return
     */
    @PutMapping("/updateUnit")
    public Result updateUnit(@RequestBody GoodsUnitRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("updateUnit begin");
            if (CommandValidator.isEmpty(request.getId())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.updateGoodsUnit(request), result);
            log.info("updateUnit end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateUnit error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询全部单位
     *
     * @return
     */
    @GetMapping("/unitList")
    public Result unitList(String type, Integer page_num, Integer page_size) {
        //token校验
        Result result = null;
        try {
            log.info("unitList begin");
            if (CommandValidator.isEmpty(type)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.findAllUnits(page_num, page_size, type));
            log.info("unitList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("unitList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询单位详情
     *
     * @return
     */
    @GetMapping("/unit/{id}")
    public Result getUnitDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            log.info("getUnitDetail begin");
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.getGoodsUnitDetail(id));
            log.info("getUnitDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getUnitDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 添加商品库存
     *
     * @return
     */
    @PostMapping("/addStock")
    public Result addStock(@RequestBody GoodsStockRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("addStock begin");
            if (CommandValidator.isEmpty(request.getGoodsSn(), request.getSpecIdList(), request.getStockNum(),
                    request.getCostPrice(), request.getSellPrice(), request.getWarnRatio())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.insertStock(request), result);
            log.info("addStock end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addStock error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 修改商品库存
     *
     * @return
     */
    @PutMapping("/updateStock")
    public Result updateStock(@RequestBody GoodsStockRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("updateStock begin");
            if (CommandValidator.isEmpty(request.getId())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsExtensionService.updateStock(request), result);
            log.info("updateStock end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateStock error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 复合查询
     * 商品库存
     *
     * @return
     */
    @PostMapping("/getAllStock")
    public Result getAllStock(@RequestBody StockSelect request) {
        //token校验
        Result result = null;
        try {
            log.info("getAllStock begin");
            if (CommandValidator.isEmpty(request.getType())) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.findAllGoodsStock(request));
            log.info("getAllStock end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getAllStock error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 根据ID查询库存详情
     *
     * @return
     */
    @GetMapping("/stock/{id}")
    public Result stockDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            log.info("stockDetail begin");
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsExtensionService.getStockById(id));
            log.info("stockDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("stockDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 添加商品
     *
     * @return
     */
    @PostMapping("/addGoods")
    public Result addGoods(@RequestBody GoodsRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("addGoods begin");
            if (CommandValidator.isEmpty(request.getBrandId(), request.getSupplyId(), request.getUnitId(),
                    request.getFirstTypeId(), request.getGoodsName(), request.getStockList())) {
                return CommandValidator.paramEmptyResult();
            }
            List<GoodsStockRequest> stockList = request.getStockList();
            if (stockList != null && !stockList.isEmpty()) {
                for (GoodsStockRequest request1: stockList) {
                    if (request1.getCostPrice() < 0 ||  request1.getSellPrice() < 0 || request1.getStockNum() <= 0) {
                        return CommandValidator.paramFailuredResult(ResultConsts.PARAM_MUST_POSIT_NUM);
                    }
                }
            }
            result = ResultUtil.getVaildResult(goodsService.insertGood(request), result);
            log.info("addGoods end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addGoods error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 后台-运营查询商品明细
     *
     * @return
     */
    @GetMapping("/{id}")
    public Result goodsDetail(@PathVariable Long id) {
        //token校验
        Result result = null;
        try {
            log.info("goodsDetail begin");
            if (CommandValidator.isEmpty(id)) {
                return CommandValidator.paramEmptyResult();
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            GoodsDto goodsDetailForIndex = goodsService.getGoodsDetail(id);
            result.setData(goodsDetailForIndex);
            log.info("goodsDetail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("goodsDetail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 前台-用户查询商品明细
     *
     * @return
     */
    @GetMapping("/detail")
    public Result indexGoodsDetail(String token, Long goods_id) {
        //token校验
        Result result = null;
        try {
            log.info("detail begin");
            if (CommandValidator.isEmpty(token, goods_id)) {
                return CommandValidator.paramEmptyResult();
            }
            if (!tokenValidator.checkToken(token)) {
                log.info("visitList 【Invaild token!】");
                return tokenValidator.invaildTokenFailedResult();
            }
            long uid = tokenValidator.getUidByToken(token);
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsService.getGoodsDetailForIndex(goods_id));
            GoodsVisitRequest request = new GoodsVisitRequest();
            request.setUid(uid);
            request.setGoodsId(goods_id);
            Future<String> future = asyncService.userVisit(request);
            if (future.isDone()) {
                log.info("Async userVisit Done rollback={}", future.get());
            }
            log.info("detail end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("detail error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 复合查询全部商品
     *
     * @return
     */
    @PostMapping("/goodsList")
    public Result goodsList(@RequestBody GoodsSelect select) {
        //token校验
        Result result = null;
        try {
            log.info("goodsList begin");
            if (CommandValidator.isEmpty(select.getType())) {
                return CommandValidator.paramEmptyResult();
            }
            String goodsName = select.getGoodsName();
            if (!StringUtils.isBlank(goodsName)) {
                //搜索A B -> AB
                select.setGoodsName(goodsName.replaceAll(" ", ""));
            }
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(goodsService.getAllGoods(select));
            log.info("goodsList end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("goodsList error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


    /**
     * 修改商品
     *
     * @return
     */
    @PutMapping("/updateGood")
    public Result updateGood(@RequestBody GoodsRequest request) {
        //token校验
        Result result = null;
        try {
            log.info("updateGood begin");
            if (CommandValidator.isEmpty(request.getId())) {
                return CommandValidator.paramEmptyResult();
            }
            result = ResultUtil.getVaildResult(goodsService.updateGood(request), result);
            log.info("updateGood end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateGood error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 导出商品列表为excel
     *
     * @return
     */
    @GetMapping("/exportGoods")
    public Result exportGoods(String type, HttpServletResponse response) {
        //token校验
        Result result = null;
        try {
            log.info("exportGoods begin type={}", type);
            if (StringUtils.isBlank(type) || (!"xls".equals(type.toLowerCase()) && !"xlsx".equals(type.toLowerCase()) && !"pdf".equals(type.toLowerCase()))) {
                return CommandValidator.paramEmptyResult();
            }
            String fileName = DateUtils.getShortString() + "GoodsList";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "." + type + "\"");
            response.setContentType("application/octet-stream;charset=UTF-8");
            GoodsSelect select = new GoodsSelect();
            select.setType("all");
            PagedList<GoodsDto> allGoods = goodsService.getAllGoods(select);
            ExcelUtils.getInstance().exportObjects2Excel(allGoods.getList(), GoodsDto.class, true, "商品列表", true, response.getOutputStream());
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.RESPONSE_SUCCEED_MSG);
            result.setData(ResultConsts.RESPONSE_SUCCEED_MSG);
            log.info("exportGoods end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("exportGoods error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }




    /**
     * 添加商品运费
     *
     * @return
     */
    @PostMapping("/addGoodsCash")
    public Result addGoodsCash(@RequestBody ExpressCash request) {
        //token校验
        Result result = null;
        try {
            log.info("addGoodsCash begin");
            result = ResultUtil.getVaildResult(goodsExtensionService.addGloableExpressCash(request), result);
            log.info("addGoodsCash end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addGoodsCash error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }

    /**
     * 查询商品运费
     *
     * @return
     */
    @GetMapping("/goodsCash")
    public Result goodsCash() {
        //token校验
        Result result = null;
        try {
            log.info("goodsCash begin");
            result = new Result(ResultConsts.REQUEST_SUCCEED_STATUS, ResultConsts.REQUEST_SUCCEED_STATUS);
            result.setData(goodsExtensionService.getExpressCash());
            log.info("goodsCash end");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("goodsCash error {}", e.getMessage());
            result = new Result(ResultConsts.REQUEST_FAILURE_STATUS, ResultConsts.SERVER_ERROR);
        }
        return result;
    }


}
