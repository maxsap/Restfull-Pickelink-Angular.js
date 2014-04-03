package com.gr.project.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Static Utility class that has public threadlocals
 * @author Anil Saldhana
 * @since May 31, 2013
 */
public class ThreadLocalUtils {
    public static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();
    public static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();
}