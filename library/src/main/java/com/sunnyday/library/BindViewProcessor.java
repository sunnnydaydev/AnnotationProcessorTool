package com.sunnyday.library;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sunnyday.library.Utils.ElementUtil;
import com.sunnyday.library.Utils.StringUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by zb on 2020/6/12 11:08
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements mElementUtil;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtil = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> typeSet = new HashSet<>();
        typeSet.add(BindView.class.getCanonicalName());
        return typeSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectBindViewAnnotations(roundEnvironment);
        return false;
    }

    /**
     * deal with element
     *
     * @param roundEnvironment roundEnvironment
     */
    private boolean collectBindViewAnnotations(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if (null == elementSet || elementSet.isEmpty()) {
            tips(Diagnostic.Kind.NOTE, " no @BindView annotation find in your code.");
            return false;
        }
        Map<TypeElement, Map<Integer, VariableElement>> typeElementMapMap = new HashMap<>();
        // 1、遍历所有的注解元素，把元素的信息放入typeElementMapMap
        for (Element element : elementSet) {
            if (element.getKind() != ElementKind.FIELD) {
                tips(Diagnostic.Kind.ERROR, "only field of class can use @%s annotation.", BindView.class.getSimpleName());
                return false;
            }
            VariableElement variableElement = (VariableElement) element;//获得作用的成员字段
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();// 获得字段所处的类

            //取typeElementMapMap中的value（variableElementMap）,为null时填充数据。
            Map<Integer, VariableElement> variableElementMap = typeElementMapMap.get(typeElement);
            if (variableElementMap == null) {
                variableElementMap = new HashMap<>();
                typeElementMapMap.put(typeElement, variableElementMap);
            }
            // 获得注解元素id值,添加到variableElementMap
            int viewId = variableElement.getAnnotation(BindView.class).value();
            variableElementMap.put(viewId, variableElement);
        }
        //2、遍历typeElementMapMap 进行信息处理
        for (TypeElement typeElement : typeElementMapMap.keySet()) {
            //根据key取typeElementMapMap得value
            Map<Integer, VariableElement> variableElementMap = typeElementMapMap.get(typeElement);
            String pkgName = ElementUtil.getPkgName(mElementUtil, typeElement);
            JavaFile javaFile = JavaFile.builder(pkgName, generateClassByPoet(typeElement, variableElementMap)).build(); // 获得个java文件
            try {
                javaFile.writeTo((File) processingEnv.getFiler());//  写文件
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * 生成java类
     *
     * @param typeElement        这里可以认为元素为 activity（上文字段的getEnclosingElement获得的）
     * @param variableElementMap 注解作用的字段值和字段元素封成的集合
     */
    private TypeSpec generateClassByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {

        return TypeSpec
                .classBuilder(ElementUtil.getEnclosingClassName(typeElement) + "ViewBinding")//生成类名：XxxActivity+ViewBinding
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodByPoet(typeElement, variableElementMap)) //为类添加方法
                .build();
    }

    private MethodSpec generateMethodByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
        String parameter = "_" + StringUtil.toLowerCaseFirstChar(className.simpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(className, parameter);
        for (int viewId : variableElementMap.keySet()) {
            VariableElement variableElement = variableElementMap.get(viewId);
            // 被注解的字段名
            String filedName = variableElement.getSimpleName().toString();
            // 被注解的字段类型
            String filedType = variableElement.asType().toString();
            String text = "{0}.{1}=({2})(({0}).findViewById({3}));\n";// 取后面的站位符号
            builder.addCode(MessageFormat.format(text, parameter, filedName, filedType, String.valueOf(viewId)));
        }

        return builder.build();
    }

    /**
     * red msg log, watch in "AS-->Build--> Build OutPut"
     */
    private void tips(Diagnostic.Kind kind, String msg, Object... args) {
        mMessager.printMessage(kind, String.format(msg, args));
    }
}
