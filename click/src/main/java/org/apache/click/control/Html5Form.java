/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.click.control;

import java.util.List;
import org.apache.click.util.HtmlStringBuffer;
import org.apache.click.util.ContainerUtils;

/**
 * Html5Form de-encapsulates the native Apache Click Form component from HTML
 * tables. It provides a semantic structure purely based on modern block-level
 * DIVs and labels, allowing immediate integration with modern utility CSS
 * frameworks like Bulma or Tailwind.
 *
 * @since Click 2.7.0-JDK17
 */
public class Html5Form extends Form {

    private static final long serialVersionUID = 1L;

    public Html5Form() {
        super();
    }

    public Html5Form(String name) {
        super(name);
    }

    /**
     * Sobrescreve o ponto de entrada principal de renderização do formulário.
     * Quebra o fluxo antigo que iniciava a tag &lt;table&gt;.
     */
    @Override
    public void render(HtmlStringBuffer buffer) {
        final boolean process = getContext().getRequest().getMethod().equalsIgnoreCase(getMethod());
        List<Field> formFields = ContainerUtils.getInputFields(this);

        // Renderiza a abertura da tag nativa <form> e os tokens/hidden fields internos do Click
        renderHeader(buffer, formFields);

        // ABORDAGEM HTML5: Substituição da tag <table> por uma div container semântica
        buffer.append("""
            <div class="click-html5-form" id="%s-form">
            """.formatted(getId()));

        // Renderização sequencial limpa em blocos, ignorando os posicionamentos de tabela originais
        renderHtml5Errors(buffer, process);
        renderHtml5Fields(buffer, formFields);
        renderHtml5Buttons(buffer);

        buffer.append("</div>\n");

        // Renderiza o fechamento da tag </form>
        renderTagEnd(formFields, buffer);
    }

    /**
     * Renderiza os erros globais e de campos do formulário em formato de
     * notificação limpa. Corrigido para utilizar as APIs nativas getError() e
     * getErrorFields() do Apache Click.
     * 
     * @param buffer the string buffer to render the errors to
     * @param processed the flag indicating whether has been processed
     */
    private void renderHtml5Errors(HtmlStringBuffer buffer, boolean processed) {
        // Verifica se o formulário foi processado e se possui erro global ou campos inválidos
        if (processed && (getError() != null || !getErrorFields().isEmpty())) {
            buffer.append("""
                <div class="form-errors-global-container" role="alert">
                """);

            // 1. Renderiza o Erro Global do Formulário (se houver)
            if (getError() != null) {
                buffer.append("""
                        <div class="form-error-item form-error-global">%s</div>
                    """.formatted(getError()));
            }

            // 2. Renderiza os erros individuais dos campos no bloco de sumário (estilo Click clássico)
            List<Field> errorFields = getErrorFields();
            for (Field field : errorFields) {
                // Evita duplicar mensagens caso o erro do campo esteja nulo por algum motivo
                if (field.getError() == null) {
                    continue;
                }

                // Mantém o comportamento original do Click de injetar o link JavaScript para foco do campo
                buffer.append("""
                        <div class="form-error-item">
                            <a class="error" href="javascript:%s">%s</a>
                        </div>
                    """.formatted(field.getFocusJavaScript(), field.getError()));
            }

            buffer.append("</div>\n");
        }
    }

    /**
     * Substitui o método renderFields() legado que injetava tags &lt;tr&gt; e
     * &lt;td&gt;. Cria uma árvore de DIVs com classes utilitárias limpas.
     * Corrigido para total compatibilidade com as APIs internas do Apache
     * Click.
     * 
     * @param buffer the HTML string buffer to render to
     * @param fields the list of form fields
     */
    private void renderHtml5Fields(HtmlStringBuffer buffer, List<Field> fields) {
        for (Field field : fields) {
            // CORREÇÃO 1: Usa a verificação interna correta para pular campos ocultos ou botões
            // Campos ocultos (HiddenFields) já são renderizados separadamente no método renderHeader() nativo
            if (field instanceof Button) {
                continue;
            }

            // Abertura do grupo do campo (Equivalente ao antigo <tr>)
            buffer.append("""
                <div class="form-field-group">
                """);

            // Renderização da Label (Equivalente ao antigo <td class="label">)
            if (field.getLabel() != null) {
                String requiredMarker = "";

                // CORREÇÃO 2: Remove a flag isShowRequiredMarker() inexistente.
                // Usamos a verificação nativa field.isRequired() e buscamos o caractere '*'
                // direto do mapa de mensagens padrão do Click, preservando a internacionalização.
                if (field.isRequired()) {
                    requiredMarker = """
                        <span class="form-required-marker">%s</span>""".formatted(getMessage("label-required-suffix"));
                }

                buffer.append("""
                    <label class="form-label" for="%s">%s%s</label>
                    """.formatted(field.getId(), field.getLabel(), requiredMarker));
            }

            // Renderização do Input Wrapper (Equivalente ao antigo <td> do controle)
            buffer.append("""
                    <div class="form-control-wrapper">
                """);

            // O próprio controle filho (ex: TextField) injeta seu HTML nativo puro (<input type="text">)
            field.render(buffer);

            // Injeta dinamicamente mensagens de erro individuais logo abaixo do input correspondente
            if (field.getError() != null) {
                buffer.append("""
                        <span class="form-error-msg">%s</span>
                    """.formatted(field.getError()));
            }

            // Fecha as estruturas de contenção do campo
            buffer.append("""
                    </div>
                </div>
                """);
        }
    }

    /**
     * Renderiza a seção de botões de ação de forma horizontal isolada. O método
     * getButtonList() é público na classe pai e compila perfeitamente.
     * 
     * @param buffer the StringBuffer to render to
     */
    private void renderHtml5Buttons(HtmlStringBuffer buffer) {
        List<Button> buttons = getButtonList();
        if (!buttons.isEmpty()) {
            buffer.append("""
                <div class="form-buttons-actions">
                """);

            for (Button button : buttons) {
                button.render(buffer);
            }

            buffer.append("</div>\n");
        }
    }

}
