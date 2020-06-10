package com.sunnyday.library;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sunnyDay on 2020/6/10
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements mElementsUtil;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementsUtil = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 1、获得BindView注解的所有元素集合
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 2、准备可集合
        Map<TypeElement, Map<Integer, VariableElement>> typeElementMap = new HashMap<>();
        // 3、遍历元素
        for (Element element : elementSet) {
            VariableElement variableElement = (VariableElement) element;//我们的BindView只作用于字段，所以这里的element就是VariableElement
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();//获得Activity元素

            // 4、集合数据填充
            Map<Integer, VariableElement> variableElementMap = typeElementMap.get(typeElement);
            if (null == variableElementMap) {
                variableElementMap = new HashMap<>();
                typeElementMap.put(typeElement, variableElementMap);
            }
            // 获得注解作用的字段值（集合数据填充完毕）
            BindView bindView = variableElement.getAnnotation(BindView.class);
            int viewId = bindView.value();
            variableElementMap.put(viewId, variableElement);
        }

        for (TypeElement typeElement : typeElementMap.keySet()) {
            Map<Integer, VariableElement> variableElementMap = typeElementMap.get(typeElement);//遍历typeElementMap，通过key取出Map集合

            PackageElement packageElement = mElementsUtil.getPackageOf(typeElement);
            String pagName = packageElement.getQualifiedName().toString();
            JavaFile javaFile = null;


        }
        return false;
    }

}
