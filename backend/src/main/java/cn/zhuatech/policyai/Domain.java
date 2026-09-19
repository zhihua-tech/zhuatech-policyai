/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.policyai;
import org.springframework.stereotype.Component;
import java.util.*;
import java.time.*;
import static cn.zhuatech.policyai.Model.*;
import static cn.zhuatech.policyai.Engine.*;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 private final InsightProvider insight;
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Domain(InsightProvider insight){this.insight=insight;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String text(Row r,String key){return txt(r.data(),key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){
  if(module.equals("policies"))require(e.all(u,"policies").stream().noneMatch(x->text(x,"title").equalsIgnoreCase(txt(d,"title"))&&text(x,"versionName").equalsIgnoreCase(txt(d,"versionName"))),"相同制度版本已存在");
  if(module.equals("controls"))require(e.all(u,"controls").stream().noneMatch(x->text(x,"name").equalsIgnoreCase(txt(d,"name"))),"控制项名称已存在");
  if(module.equals("assessments"))e.ref(u,d,"policy","policies");
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("policies"))require(e.all(u,"policies").stream().noneMatch(x->!x.id().equals(r.id())&&text(x,"title").equalsIgnoreCase(txt(d,"title"))&&text(x,"versionName").equalsIgnoreCase(txt(d,"versionName"))),"相同制度版本已存在");
  if(r.module().equals("controls"))require(e.all(u,"controls").stream().noneMatch(x->!x.id().equals(r.id())&&text(x,"name").equalsIgnoreCase(txt(d,"name"))),"控制项名称已存在");
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  switch(r.module()+"."+action){
   case "policies.publish" -> d.put("publishedAt",Instant.now().toString());
   case "policies.retire" -> {require(e.all(u,"assessments").stream().noneMatch(x->text(x,"policy").equals(r.id())&&x.state().equals("REVIEW")),"仍有待复核评估，不得停用制度");d.put("retireReason",txt(i,"reason"));}
   case "assessments.analyze" -> {
    Row policy=e.ref(u,d,"policy","policies");require(policy.state().equals("PUBLISHED")&&!date(policy.data(),"effectiveDate").isAfter(LocalDate.now()),"只能分析已发布且已生效的制度");
    var findings=insight.analyze(text(policy,"content"),txt(d,"scope"),e.all(u,"controls").stream().filter(x->x.state().equals("ACTIVE")).toList());
    int score=Math.min(100,findings.stream().mapToInt(InsightProvider.Finding::score).sum());
    for(var f:findings)e.ledger(u,"findings","RECORDED",Map.of("assessment",r.id(),"policy",policy.id(),"control",f.controlId(),"controlName",f.controlName(),"severity",f.severity(),"evidence",f.evidence(),"recommendation",f.recommendation(),"method","LOCAL_RULES_V1"));
    d.put("analysisScore",score);d.put("findingCount",findings.size());d.put("policyVersion",text(policy,"versionName"));d.put("analysisAt",Instant.now().toString());d.put("method","LOCAL_RULES_V1");
   }
   case "assessments.submit" -> require(d.containsKey("analysisAt"),"请先完成影响分析");
   case "assessments.approve" -> {d.put("approvedBy",u.username());d.put("approvedAt",Instant.now().toString());}
   case "assessments.return" -> d.put("returnReason",txt(i,"reason"));
  }
  return null;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){return Map.of("有效制度",e.all(u,"policies").stream().filter(x->x.state().equals("PUBLISHED")).count(),"待复核评估",e.all(u,"assessments").stream().filter(x->x.state().equals("REVIEW")).count(),"累计高风险发现",e.all(u,"findings").stream().filter(x->text(x,"severity").equals("HIGH")).count());}
}
