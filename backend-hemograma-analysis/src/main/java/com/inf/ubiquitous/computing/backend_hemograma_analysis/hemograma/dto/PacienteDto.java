package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto;

import java.time.LocalDate;

/**
 * DTO representando um paciente com dados demográficos para análise epidemiológica.
 */
public class PacienteDto {
    
    private String id;
    private String nome;
    private Integer idade;
    private String sexo; // "M" ou "F"
    private String regiao; // Norte, Nordeste, Centro-Oeste, Sudeste, Sul
    private String estado;
    private String cidade;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
    
    // Construtores
    public PacienteDto() {}
    
    public PacienteDto(String id, String nome, Integer idade, String sexo, 
                      String regiao, String estado, String cidade, String cpf, 
                      LocalDate dataNascimento, String telefone) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.sexo = sexo;
        this.regiao = regiao;
        this.estado = estado;
        this.cidade = cidade;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }
    
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    
    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    @Override
    public String toString() {
        return "PacienteDto{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", idade=" + idade +
                ", sexo='" + sexo + '\'' +
                ", regiao='" + regiao + '\'' +
                ", estado='" + estado + '\'' +
                ", cidade='" + cidade + '\'' +
                '}';
    }
}