package br.org.aera.financeiro.web;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class HomeController {
 @GetMapping("/") public String home(){return "redirect:/movimentacoes";}
 @GetMapping("/login") public String login(){return "login";}
}
