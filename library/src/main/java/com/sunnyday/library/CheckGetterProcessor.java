package com.sunnyday.library;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zb on 2020/6/9
 */

@AutoService(Processor.class)
public class CheckGetterProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtil;
    private Filer filer;
    private Types types;

    /**
     * 存放注解处理器的初始化代码
     *
     * @param processingEnvironment 通过参数可以获得一系列的工具类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        elementUtil = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        types = processingEnvironment.getTypeUtils();
        super.init(processingEnvironment);
    }

    /**
     * 返回注解处理器所支持的注解类型。
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(CheckGetter.class.getCanonicalName());
        return set;
    }

    /**
     * 返回注解处理器支持的java版本，通常返回你自己的java版本即可。
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 注解处理器的关键方法
     *
     * @param set              注解处理器所能处理的注解类型
     * @param roundEnvironment 用于生成抽象语法树
     *                         <p>
     *                         Element：表示程序元素接口，所有注解在目标上的元素。获得这些元素后可使用其子类处理。
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 获得所有注解作用的类元素，解析类上的注解信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            TypeElement clazzElement = (TypeElement) element; // TypeElement 代表类或者接口元素，可以获得类或者接口的信息。
            PackageElement packageElement = (PackageElement) element.getEnclosingElement();// PackageElement 代表包元素
            String fullClassName = clazzElement.getQualifiedName().toString();   //全类名
            String className = clazzElement.getSimpleName().toString();   // 类名
            String pkgName = packageElement.getQualifiedName().toString();// 包名
            String parentName = clazzElement.getSuperclass().toString();// 父类名
        }
         //获得所有注解作用的方法元素，解析方法上的注解信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)){
            ExecutableElement executableElement = (ExecutableElement) element;
            // 类型之间还可以转换
            TypeElement classElement = (TypeElement) executableElement.getEnclosingElement();
            PackageElement packageElement = elementUtil.getPackageOf(classElement);

            String pkgName = packageElement.getQualifiedName().toString();// 包名
            String fullClassName = classElement.getQualifiedName().toString(); //全类名
            String methodName = executableElement.getSimpleName().toString();//方法名

            //方法参数列表元素:一般用于获取参数类型、参数名。
            List<? extends VariableElement> methodParameters = executableElement.getParameters();
            List<String> types = new ArrayList<>();
            for (VariableElement variableElement : methodParameters) {// 遍历每个参数元素
                TypeMirror methodParameterType = variableElement.asType();
                if (methodParameterType != null) {
                    TypeVariable typeVariable = (TypeVariable) methodParameterType;
                    methodParameterType = typeVariable.getUpperBound();
                }

                String parameterName = variableElement.getSimpleName().toString(); //参数名
                String parameteKind = methodParameterType.toString(); //参数类型
                types.add(methodParameterType.toString());
            }
        }
        //解析属性上的注解，获得每个属性上的元素信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType(); //类成员类型
        }

        return true;
    }
}

/**   Element 及其子类之间的关系
 *    1、Element 为元素父类，其是一个接口

      2、getEnclosingElement 方法返回封装此 Element 的最里层元素。如下：

          VariableElement调用会返回TypeElement
          ExecutableElement调用会返回TypeElement

 package foo;     // PackageElement
 class Foo {      // TypeElement
 int a;           // VariableElement
 static int b;    // VariableElement
 Foo () {}        // ExecutableElement
 void setA (      // ExecutableElement
 int newA         // VariableElement
 ) {}
 }

 * */
