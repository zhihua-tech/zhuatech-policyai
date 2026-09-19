/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.policyai;
import org.springframework.stereotype.Component;
import java.util.*;
import static cn.zhuatech.policyai.Model.*;
import static cn.zhuatech.policyai.Engine.*;

/**
 * 可替换的制度影响分析接口。默认实现完全本地运行，不调用外部模型。
 *
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
public interface InsightProvider {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 record Finding(String controlId,String controlName,String severity,String evidence,String recommendation,int score){}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 List<Finding> analyze(String policyText,String scope,List<Row> controls);
}

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component class LocalInsightProvider implements InsightProvider {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public List<Finding> analyze(String policyText,String scope,List<Row> controls){
  String source=policyText.toLowerCase(Locale.ROOT);
  List<Finding> results=new ArrayList<>();
  for(Row control:controls){
   String matched=Arrays.stream(txt(control.data(),"keywords").split("[,;，；\\s]+")).map(String::strip).filter(x->!x.isEmpty()).filter(x->source.contains(x.toLowerCase(Locale.ROOT))).findFirst().orElse("");
   if(matched.isEmpty())continue;
   String severity=txt(control.data(),"severity");
   int score=switch(severity){case "HIGH"->40;case "MEDIUM"->25;default->10;};
   results.add(new Finding(control.id(),txt(control.data(),"name"),severity,matched,"由 "+txt(control.data(),"owner")+" 核对控制项并保留执行证据",score));
  }
  return results;
 }
}
