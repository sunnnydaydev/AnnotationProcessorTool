package com.sunnyday.library;


import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by zb on 2020/6/9 16:12
 * 注解处理器：注解处理器相关方法的练习。
 */

@AutoService(Processor.class)
public class CheckGetterProcessor extends AbstractProcessor {
    private static final String TAG = CheckGetter.class.getSimpleName();

    private Messager messager; // 用于给注解处理器报告警告、处理、提示消息。
    private Elements elementUtil; // 处理程序元素的工具类
    private Filer filer;// 给注解处理器创建文件
    private Types types;// 处理类型数据的工具类

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
//        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
//            TypeElement clazzElement = (TypeElement) element; // TypeElement 代表类或者接口元素，可以获得类或者接口的信息。
//
//         //   PackageElement packageElement = (PackageElement) element.getEnclosingElement();// PackageElement 代表包元素
//
//            String fullClassName = clazzElement.getQualifiedName().toString();   //全类名
//            String className = clazzElement.getSimpleName().toString();   // 类名
//            String pkgName = clazzElement.getEnclosingElement().toString();// 包名
//            String parentName = clazzElement.getSuperclass().toString();// 父类名
//
//            note("--------------------------------process class info ----------------------------");
//            note("fullClassName:" + fullClassName);
//            note("className:" + className);
//            note("pkgName:" + pkgName);
//            note("parentName:" + parentName);
//
//        }

        //获得所有注解作用的方法元素，解析方法上的注解信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;// 由于作用于方法所以可以直接强转方法类型
            TypeElement classElement = (TypeElement) executableElement.getEnclosingElement(); // 类型之间还可以转换，方法的外层即类
            PackageElement packageElement = elementUtil.getPackageOf(classElement);// 根据元素直接获得包类型

            String pkgName = packageElement.getQualifiedName().toString();// 包名
            String fullClassName = classElement.getQualifiedName().toString(); //全类名
            String methodName = executableElement.getSimpleName().toString();//方法名
            note("--------------process method info-----------");
            note("pkgName:" + pkgName);
            note("fullClassName:" + fullClassName);
            note("methodName:" + methodName);

            //方法参数列表元素:一般用于获取参数类型、参数名。
            List<? extends VariableElement> methodParameters = executableElement.getParameters();
            List<String> types = new ArrayList<>();
            for (VariableElement variableElement : methodParameters) {// 遍历每个参数元素
                TypeMirror methodParameterType = variableElement.asType();

                String parameterName = variableElement.getSimpleName().toString(); //参数名
                String parameterKind = methodParameterType.toString(); //参数类型
                note("------------process method param-------------");
                note("parameterName:" + parameterName);
                note("parameterKind:" + parameterKind);
                note("typeVariable:" + methodParameterType);


            }
            note("types:" + types.toString());
        }


        //解析属性上的注解，获得每个属性上的元素信息。
//        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
//            VariableElement variableElement = (VariableElement) element;
//            TypeMirror typeMirror = variableElement.asType();
//
//            CheckGetter checkGetter = element.getAnnotation(CheckGetter.class);
//           // checkGetter.value() 注解定义值时这里可以取出
//
//            note("--------------------------------process field info ----------------------------");
//            note("typeMirror:" + typeMirror.toString()); // 变量类型全名（带包名）
//            note("variableElement.getSimpleName:" + variableElement.getSimpleName()); // 变量名
//            note("variableElement.getEnclosingElement:" + variableElement.getEnclosingElement());

//
//        }

        return true;
    }

    /**
     * @param msg 要打印的信息
     * @function Build->Build Output 窗口查看打印消息
     */
    private void note(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}


