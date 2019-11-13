package cn.mzhong.kbus.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * TODO<br>
 * 创建时间： 2019/11/13 10:18
 *
 * @author mzhong
 * @version 1.0
 */
@RequestMapping(value = "/")
@RestController
public class HomeController {

    @GetMapping(value = "/header.html")
    ResponseEntity<String> home(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headerBuilder = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headerBuilder.append("<tr>")
                    .append("<td>").append(name).append("</td>")
                    .append("<td>").append(value).append("</td>")
                    .append("</tr>");
        }
        String html = "<html>"
                + "<head>"
                + "<title>测试页面</title>"
                + "</head>"
                + "<body>"
                + "<h1>这是测试页面</h1>"
                + "<table style=\"text-align: left;\" border=\"1\">"
                + "<tr>"
                + "<th>名称</th><th>值</th>"
                + "</tr>"
                + headerBuilder.toString()
                + "</table>"
                + "</body>"
                + "</html>";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(html, httpHeaders, HttpStatus.OK);
    }
}
