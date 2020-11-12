package springboot_redis.demo;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.tianshu.annotation.OnCall;
import com.alibaba.tianshu.callback.base.AbstractFormAfterSubmitBase;
import com.alibaba.tianshu.callback.data.FormAfterSubmitRequest;
import com.alibaba.tianshu.callback.data.FormAfterSubmitResponse;
import com.alibaba.tianshu.callback.data.FormBeforeSubmitValidityCheckRequest;
import com.alibaba.tianshu.callback.data.model.FormAction;
import com.alibaba.tianshu.callback.data.model.FormContext;
import com.alibaba.tianshu.callback.data.model.FormData;
import com.alibaba.tianshu.callback.data.model.UserData;
import com.alibaba.tianshu.exception.BusinessWarnException;
import com.alibaba.tianshu.supply.ServiceSupply;
import com.alibaba.work.tianshu.api.common.param.ServiceResult;
import com.alibaba.work.tianshu.api.form.param.DeleteFormDataParam;
import com.alibaba.work.tianshu.api.form.param.GetFormDataByIdParam;
import com.alibaba.work.tianshu.api.form.param.SaveFormDataParam;
import com.alibaba.work.tianshu.api.form.param.SearchFormDataParam;
import com.alibaba.work.tianshu.api.form.param.UpdateFormDataParam;
import com.alibaba.work.tianshu.api.model.dto.PageDto;
import com.alibaba.work.tianshu.api.form.vo.FormInstanceVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Controller;

@OnCall(id = "getTestParms", name = "test数据", description = "test数据")
public class ExampleOnFromAfterSubmit extends AbstractFormAfterSubmitBase {

    private static final Logger logger = LoggerFactory.getLogger(ExampleOnFromAfterSubmit.class);

    @Autowired
    private ServiceSupply serviceSupply;

    public FormAfterSubmitResponse execute(FormAfterSubmitRequest formAfterSubmitRequest) {
        String uuid = formAfterSubmitRequest.getFormContext().getFormUuid();//当前表单id
        String instId = formAfterSubmitRequest.getFormContext().getFormInstId();//当前表单实例id
        Object value = formAfterSubmitRequest.getFormDataAfter().getData().get("textField_kh1bwt5y");//根据当前表单组件唯一id获取对应组件值
        if (value == null) {
            throw new BusinessWarnException("数据不存在");
        }

        //---------------查询条件-------------
        Map<String, Object> searchMap = new HashMap<>();//添加查询所需要的条件
        searchMap.put("textField_kh1cahwz", value);//"textField_kh1cahwz"需要查询的表单组件的唯一id
        String searchField = JSON.toJSONString(searchMap);//Map 到 JSON 字符串转换
        //---------------查询条件-------------
        String targetUuid = "FORM-PC766J816CPKHE5SY40ZVTYF7N7R1LFEAC1HKL";//目标表单id
        List<String> instanIdList = SearchDataValue(formAfterSubmitRequest,targetUuid,searchField);//查询：根据当前表单值，查询获取表单的实例id List集合
        //遍历根据表单实列id获取对应表单的值
        for (String instanId : instanIdList) {//instanId是表单实例id
            GetFormDataByIdParam getFormDataByIdParam = new GetFormDataByIdParam();
            getFormDataByIdParam.setUserId(formAfterSubmitRequest.getFormContext().getLoginUser().getUserId());
            getFormDataByIdParam.setFormInstId(instanId);
            ServiceResult<FormInstanceVo> formResult = serviceSupply.getFormDataService().getFormDataById(getFormDataByIdParam);//获取对应表单数据
            FormInstanceVo vo = formResult.getResult();//获取表单内容
            Object peopleIdObj = (Object)vo.getFormData().get("numberField_kh1cahx0");//获取对应表单组件的值
        }

        //新增
        Map<String, Object> formDataSave = new HashMap<>();
        formDataSave.put("textField_kh1ngi1x", "value");//添加目标表单的组件对应的String值
        formDataSave.put("numberField_kh1ngi1y", 123);//添加目标表单的组件对应的int值
        formDataSave.put("employeeField_k341rdyg",String.format("[\"%s\"]",formAfterSubmitRequest.getFormContext().getLoginUser().getUserId()));//帮助文档提供的添加内容
        saveJob(formAfterSubmitRequest,formDataSave,targetUuid);

        //更新
        String targetInstId = "FINST-TG866W71JDQKQ6202KX9AC9DR1H43BCD7T2HK21";//表单实列id
        Map<String, Object> formDataUpdate = new HashMap<>();
        formDataUpdate.put("textField_kh1ngi1x", "value");//更新目标表单的组件对应的String值
        formDataUpdate.put("numberField_kh1ngi1y", 123);//更新目标表单的组件对应的int值
        formDataUpdate.put("employeeField_k341rdyg",String.format("[\"%s\"]",formAfterSubmitRequest.getFormContext().getLoginUser().getUserId()));//帮助文档提供的添加内容
        updateJob(formAfterSubmitRequest,formDataUpdate,targetInstId);

        //删除
        deleteJob(formAfterSubmitRequest,targetInstId);

        return null;
    }

    //查询
    private List<String> SearchDataValue(FormAfterSubmitRequest arg0,String uuid,String searchField){
        SearchFormDataParam searchFormDataParam = new SearchFormDataParam();//创建查询对象
        searchFormDataParam.setUserId(arg0.getFormContext().getLoginUser().getUserId());//当前登录人id
        searchFormDataParam.setFormUuid(uuid);//设置查询表单id
        searchFormDataParam.setSearchFieldJson(searchField);//searchField为JSON字符串，即查询条件
        ServiceResult<PageDto<String>> result = serviceSupply.getFormDataService().searchFormDataIds(searchFormDataParam);//查询结果
        if (!result.isSuccess()) {//判断查询结果
            throw new BusinessWarnException(result.getErrorMsg());
        }
        return result.getResult().getData(); //获取查询结果里的data，即表单的实例id,并返回
    }

    //新增
    private void saveJob(FormAfterSubmitRequest arg0, Map<String,Object> formData,String uuid){
        SaveFormDataParam saveFormData = new SaveFormDataParam();
        //---------------------------新增设置-------
        saveFormData.setUserId(arg0.getFormContext().getLoginUser().getUserId());//当前登录人id
        saveFormData.setFormUuid(uuid); //设置目标表单id
        saveFormData.setFormDataJson(JSON.toJSONString(formData));//新增需的表单内容
        //--------------------------------------
        ServiceResult<String>result= serviceSupply.getFormDataService().saveFormData(saveFormData);//执行并返回结果
        if (!result.isSuccess()) {
            throw new BusinessWarnException(result.getErrorMsg());
        }
    }

    //更新
    private void updateJob(FormAfterSubmitRequest arg0, Map<String,Object> formData,String instId){
        UpdateFormDataParam updateFormData = new UpdateFormDataParam();
        //------------------- --------更新设置-------
        updateFormData.setUserId(arg0.getFormContext().getLoginUser().getUserId()); //当前登录人id
        updateFormData.setFormInstId(instId);//更新表单需的实列id
        updateFormData.setUpdateFormDataJson(JSON.toJSONString(formData));
        //--------------------------------------
        ServiceResult<Void> result = serviceSupply.getFormDataService().updateFormData(updateFormData);//执行并返回结果
        if (!result.isSuccess()) {
            throw new BusinessWarnException(result.getErrorMsg());
        }
    }

    //删除
    private void deleteJob(FormAfterSubmitRequest arg0,String instId){
        DeleteFormDataParam deleteFormData = new DeleteFormDataParam();
        //  --------删除设置-------
        deleteFormData.setUserId(arg0.getFormContext().getLoginUser().getUserId());//当前登录人id
        deleteFormData.setFormInstId(instId);//删除表单需的实列id
        //--------------------------------------
        ServiceResult<Void> result = serviceSupply.getFormDataService().deleteFormData(deleteFormData);//执行并返回结果
        if (!result.isSuccess()) {
            throw new BusinessWarnException(result.getErrorMsg());
        }
    }


}
