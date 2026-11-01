package br.org.aera.financeiro.web;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/config")
public class ConfigController {
 private final ContaRepository contas; private final CategoriaRepository categorias;
 public ConfigController(ContaRepository contas,CategoriaRepository categorias){this.contas=contas;this.categorias=categorias;}
 @GetMapping public String config(Model model){model.addAttribute("contas",contas.findAllByAtivaTrueOrderByNomeAsc());model.addAttribute("categorias",categorias.findAllByAtivaTrueOrderByNomeAsc());return "config";}
 @PostMapping("/contas") public String conta(@RequestParam String nome,RedirectAttributes flash){
  if(nome.isBlank()||nome.length()>100){flash.addFlashAttribute("erro","Nome inválido.");return "redirect:/config";}
  try{contas.saveAndFlush(new Conta(nome.trim()));}catch(DataIntegrityViolationException ex){flash.addFlashAttribute("erro","Conta já existe.");}
  return "redirect:/config";
 }
 @PostMapping("/categorias") public String categoria(@RequestParam String nome,RedirectAttributes flash){
  if(nome.isBlank()||nome.length()>100){flash.addFlashAttribute("erro","Nome inválido.");return "redirect:/config";}
  try{categorias.saveAndFlush(new Categoria(nome.trim()));}catch(DataIntegrityViolationException ex){flash.addFlashAttribute("erro","Categoria já existe.");}
  return "redirect:/config";
 }
}
