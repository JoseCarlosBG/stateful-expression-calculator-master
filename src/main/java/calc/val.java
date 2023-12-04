package calc;

import javax.servlet.*;
import javax.servlet.Filter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/calc/*")
public class val extends HttpServlet implements Filter {
    private Map<String,String> var = new LinkedHashMap<>(); //List of parameters

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,String> val = new LinkedHashMap<>();

        for (Map.Entry<String,String> entry: var.entrySet()){//Local copy of parameters
            val.put(entry.getKey(), entry.getValue());
        }

        String equation = val.get(val.keySet().iterator().next()); //Equation
        val.remove(val.keySet().iterator().next()); //Remove equation from local copy

        for (Map.Entry<String,String> entry: val.entrySet())
            equation = equation.replace(entry.getKey(), entry.getValue());

        response.setContentType("text/html");//setting the content type
        PrintWriter pw;//get the stream to write the data
        pw = response.getWriter();

        pw.print(eval(equation));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().split("/");
        String val = pathInfo[1];
        var.put(val,new String(request.getInputStream().readAllBytes())); //Add parameter to map
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().split("/");
        String val = pathInfo[1];
        var.remove(val); //Removes parameter from the map
    }

    private int eval(String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            int parse() {
                nextChar();
                int x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            int parseExpression() {
                int x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            int parseTerm() {
                int x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            int parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();

                int x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing: ')'");
                } else if (ch >= '0' && ch <= '9') {
                    while (ch >= '0' && ch <= '9') nextChar();
                    x = Integer.parseInt(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing: ')'");
                    } else x = parseFactor();
                } else throw new RuntimeException("Unexpected: " + (char) ch);
                if (eat('^')) x = (int) Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }
}