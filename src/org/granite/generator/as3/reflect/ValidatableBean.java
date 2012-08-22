package org.granite.generator.as3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ValidatableBean {
	
	private final Class<?> type;
	private final String metaAnnotationName;
	private final List<String> specialAnnotationNames;
	private final Map<String, String> nameConversions;
	
	public ValidatableBean(Class<?> type, String metaAnnotationName, List<String> specialAnnotationNames, Map<String, String> nameConversions) {
		this.type = type;
		this.metaAnnotationName = metaAnnotationName;
		this.specialAnnotationNames = specialAnnotationNames;
		this.nameConversions = nameConversions;
	}
	
	public void buildConstraints(Map<String, JavaProperty> properties, Map<JavaProperty, List<JavaConstraint>> constraints) {
		Class<? extends Annotation> metaAnnotationClass = loadMetaAnnotationClass(type, metaAnnotationName);
		if (metaAnnotationClass == null)
			return;
	    
	    // Collect validation annotations
	    for (JavaProperty property : properties.values()) {
	    	List<JavaConstraint> javaConstraints = new ArrayList<JavaConstraint>();
	    	
	    	List<Annotation> constraintAnnotations = new ArrayList<Annotation>();
	    	for (Annotation annotation : property.getDeclaredAnnotations()) {
				Class<? extends Annotation> annotationClass = annotation.annotationType();
				
				if (annotationClass.isAnnotationPresent(metaAnnotationClass) || specialAnnotationNames.contains(annotationClass.getName()))
					constraintAnnotations.add(annotation);
				else {
	
					// (Spec 2.2) "...the bean validation provider treats regular annotations
					// (annotations not annotated by @Constraint) whose value element has a
					// return type of an array of constraint annotations in a special way.
					// Each element in the value array are processed by the Bean Validation
					// implementation as regular constraint annotations."
	
					Method value = null;
					try {
						value = annotationClass.getMethod("value");
					}
					catch (NoSuchMethodException e) {
					}
					
					if (value != null && value.getReturnType().isArray() &&
						value.getReturnType().getComponentType().isAnnotation() &&
						value.getReturnType().getComponentType().isAnnotationPresent(metaAnnotationClass)) {
						
						try {
	    					Annotation[] annotationList = (Annotation[])value.invoke(annotation);
	    					constraintAnnotations.addAll(Arrays.asList(annotationList));
						}
						catch (Exception e) {
							// should never happen...
						}
					}
				}
	    	}
	    	
			for (Annotation constraint : constraintAnnotations) {
				List<String[]> attributes = new ArrayList<String[]>();
	
				for (Method attribute : constraint.annotationType().getDeclaredMethods()) {
					if (Modifier.isPublic(attribute.getModifiers()) &&
						!Modifier.isStatic(attribute.getModifiers()) &&
						attribute.getParameterTypes().length == 0) {
						
						Object value = null;
						try {
							value = attribute.invoke(constraint);
						}
						catch (Exception e) {
							continue;
						}
						
						if (value != null && (!value.getClass().isArray() || Array.getLength(value) > 0))
							attributes.add(new String[]{attribute.getName(), escape(value), attribute.getReturnType().getName()});
					}
				}
				
				String constraintName = constraint.annotationType().getName();
				if (nameConversions.containsKey(constraintName))
					constraintName = nameConversions.get(constraintName);
				String packageName = constraintName.indexOf(".") > 0 ? constraintName.substring(0, constraintName.lastIndexOf(".")) : "";
				constraintName = constraintName.indexOf(".") > 0 ? constraintName.substring(constraintName.lastIndexOf(".")+1) : constraintName;
				if (nameConversions.containsKey(packageName))
					packageName = nameConversions.get(packageName);
				
				javaConstraints.add(new JavaConstraint(packageName, constraintName, attributes));
			}
	    	
	    	if (!javaConstraints.isEmpty())
	    		constraints.put(property, javaConstraints);
	    }
	}
    
    @SuppressWarnings("unchecked")
	private static Class<? extends Annotation> loadMetaAnnotationClass(Class<?> type, String metaAnnotationName) {
		try {
			return (Class<? extends Annotation>)type.getClassLoader().loadClass(metaAnnotationName);
		}
		catch (Exception e) {
			return null;
		}
    }
	
	private static String escape(Object value) {
		
		if (value.getClass().isArray()) {
			StringBuilder sb = new StringBuilder();
			
			final int length = Array.getLength(value);
			boolean first = true;
			for (int i = 0; i < length; i++) {
				Object item = Array.get(value, i);
				if (item == null)
					continue;

				if (first)
					first = false;
				else
					sb.append(", ");
				
				sb.append(escape(item, true));
			}
			
			return sb.toString();
		}
		
		return escape(value, false);
	}
	
	private static String escape(Object value, boolean array) {
		if (value instanceof Class<?>)
			return ((Class<?>)value).getName();
		
		if (value.getClass().isEnum())
			return ((Enum<?>)value).name();
		
		value = value.toString().replace("&", "&amp;").replace("\"", "&quot;").replace("\\", "\\\\");
		if (array)
			value = ((String)value).replace(",", ",,");
		return (String)value;
	}
}
