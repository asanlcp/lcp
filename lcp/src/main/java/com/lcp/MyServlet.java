package com.lcp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lcp.mvcannotation.Autowired;
import com.lcp.mvcannotation.Controller;
import com.lcp.mvcannotation.RequestMapping;
import com.lcp.mvcannotation.Service;

/**
 * Servlet implementation class MyServlet
 */
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private List<String> classNames = new ArrayList<String>();
	private Map<String, Object> beans = new HashMap<String, Object>();
	private Map<String, Object> handleMethod = new HashMap<String, Object>();
	/**
	 * Default constructor.
	 */
	public MyServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		// 1.模拟spring扫描包
		String packagePath = "com.lcp";
		scanPackage(packagePath);
		// 2.类的实例化
		classInstance();
		// 3.依赖注入
		ioc();
		//4.关系映射
		relationalMapping();
	}

	private void relationalMapping() {
		// TODO Auto-generated method stub
		if (beans.isEmpty()) {
			System.out.println("beans is empty");
			return;
		}
		for (Entry<String, Object> entry : beans.entrySet()) {
			Object obj = entry.getValue();
			if(obj.getClass().isAnnotationPresent(Controller.class)) {
				//获取requestMapping中路径值 定义在类上面的
				RequestMapping requestMapping = obj.getClass().getAnnotation(RequestMapping.class);
				String path = requestMapping.value();
				//获取方法
				Method[] methods =obj.getClass().getDeclaredMethods();
				for(Method method:methods) {
					if(method.isAnnotationPresent(RequestMapping.class)) {
						RequestMapping mathodMapping = method.getAnnotation(RequestMapping.class);
						String value= mathodMapping.value();
						//类上RequestMapping+方法上的RequestMapping  /index/test
                        handleMethod.put(path+value, method);
					}
				}
				
			}
		}
	}

	private void ioc() {
		if (beans.isEmpty()) {
			System.out.println("beans is empty");
			return;
		}
		for (Entry<String, Object> entry : beans.entrySet()) {
			Object obj = entry.getValue();
			// 获取成员属性
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field fiels : fields) {
				// 判断有没有注入批注
				if (fiels.isAnnotationPresent(Autowired.class)) {
					Autowired autowired = fiels.getAnnotation(Autowired.class);
					String value = autowired.value();
					// private属性可以获取到
					fiels.setAccessible(true);
					try {
						// 给这个属性set值
						fiels.set(obj, beans.get(value));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void classInstance() {
		if (classNames.isEmpty()) {
			System.out.println(" classNames is empty");
			return;
		}
		for (String className : classNames) {
			try {
				// com.lcp.controller.TestController.class
				String name = className.replace(".class", "");
				Class<?> clazz = Class.forName(name);
				// 扫描到某个类注解为Controller
				if (clazz.isAnnotationPresent(Controller.class)) {
					Controller controller = clazz.getAnnotation(Controller.class);
					// 完成controller实例化 放入缓存bean里面
					Object obj = clazz.newInstance();
					if (clazz.isAnnotationPresent(RequestMapping.class)) {
						RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
						String mapperValue = requestMapping.value();
						beans.put(mapperValue, obj);
					}
				}
				// 处理注解为service的类
				if (clazz.isAnnotationPresent(Service.class)) {
					Service service = clazz.getAnnotation(Service.class);
					Object obj = clazz.newInstance();
					beans.put(service.value(), obj);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void scanPackage(String packagePath) {
		// TODO Auto-generated method stub
		// 替换路径中的点
		String path = packagePath.replaceAll("\\.", "/");/// com/lcp/controller
		URL url = getClass().getClassLoader().getResource("/" + path);
		File[] files = new File(url.getPath()).listFiles();
		// 获取所有文件
		for (File file : files) {
			// 判断是不是目录
			if (file.isDirectory()) {
				scanPackage(packagePath + "." + file.getName());
			} else {
				classNames.add(packagePath + "." + file.getName());
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String url = request.getRequestURI();
		System.out.println("url:"+url);
		String contextPath = request.getContextPath();
		System.out.println("contextPath:"+contextPath);
		String path  = url.replace(contextPath, "");
		System.out.println("path:"+path);
		Method method = (Method) handleMethod.get(path);
		//执行该方法
		try {
			String[] strs =path.split("/");
			Object obj = beans.get("/"+strs[1]);
			method.invoke(obj);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
