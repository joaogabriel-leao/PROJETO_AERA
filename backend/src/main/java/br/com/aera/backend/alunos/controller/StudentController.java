package br.com.aera.backend.alunos.controller;

import br.com.aera.backend.alunos.domain.StudentStatus;
import br.com.aera.backend.alunos.dto.StudentCreateRequest;
import br.com.aera.backend.alunos.dto.StudentDetailResponse;
import br.com.aera.backend.alunos.dto.StudentPageResponse;
import br.com.aera.backend.alunos.dto.StudentStatusUpdateRequest;
import br.com.aera.backend.alunos.dto.StudentUpdateRequest;
import br.com.aera.backend.alunos.service.StudentService;
import br.com.aera.backend.common.exception.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * API de alunos em /api/alunos: /api ja e o context-path da aplicacao.
 *
 * <p>Entradas e saidas sao JSON, com nomes de campo do studentService.ts.
 * UUIDs sao strings; datas civis usam yyyy-MM-dd e createdAt usa ISO-8601
 * com offset. Erros seguem ErrorResponse (status, error, message, timestamp).
 * Validacao/formato invalido retorna 400, aluno ausente 404 e conflito de
 * persistencia 409. A listagem nao expoe documentos ou observacoes.</p>
 *
 * <p>BLOQUEADOR DE PRODUCAO: a aplicacao ainda nao possui autenticacao nem
 * autorizacao. Nao utilizar dados pessoais reais em producao antes de
 * implementar o controle de acesso. Nao ha exclusao definitiva nesta API.</p>
 */
@RestController
@RequestMapping("/alunos")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    /**
     * GET /api/alunos?search=texto&amp;status=ATIVO&amp;modality=Surf&amp;page=0&amp;size=20.
     * Busca em nome, codigo e responsavel; filtros combinados com AND.
     * Retorna 200 com content, page, size, totalElements e totalPages.
     * Pagina inicial zero; tamanho entre 1 e 100; ordenacao por codigo.
     */
    @GetMapping
    public StudentPageResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) String modality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(search, status, modality, page, size);
    }

    /** GET /api/alunos/{id}: 200 com StudentDetailResponse; 404 se nao existir. */
    @GetMapping("/{id}")
    public StudentDetailResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    /**
     * POST /api/alunos: recebe StudentCreateRequest (nome, nascimento e status
     * obrigatorios); retorna 201 com StudentDetailResponse e Location do cadastro.
     * id, code e createdAt sao gerados no servidor.
     */
    @PostMapping
    public ResponseEntity<StudentDetailResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        StudentDetailResponse response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * PATCH /api/alunos/{id}: recebe StudentUpdateRequest e retorna 200 com detalhe.
     * Campo omitido/null preserva o valor. Texto opcional vazio limpa o campo.
     * clearFields permite limpar opcionais, inclusive startDate. Campos tecnicos,
     * obrigatorios ou pedidos de alterar e limpar o mesmo campo sao rejeitados.
     */
    @PatchMapping("/{id}")
    public StudentDetailResponse update(
            @PathVariable UUID id, @Valid @RequestBody StudentUpdateRequest request) {
        return service.update(id, request);
    }

    /** PATCH /api/alunos/{id}/situacao: recebe {"status":"ATIVO"|"INATIVO"}; retorna 200 com detalhe. */
    @PatchMapping("/{id}/situacao")
    public StudentDetailResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody StudentStatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    /** Complementa o handler global somente para validacoes da entrada deste modulo. */
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(RuntimeException ex) {
        String message = ex instanceof ConstraintViolationException
                ? "Dados invalidos na requisicao." : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, HttpStatus.BAD_REQUEST.getReasonPhrase(), message));
    }

    /** Nao expoe detalhes do banco ou dados pessoais em conflitos de persistencia. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, HttpStatus.CONFLICT.getReasonPhrase(),
                        "O cadastro conflita com dados existentes."));
    }
}
