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

import java.util.Map;
import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.click.MockContext;
import org.apache.click.Page;
import org.apache.click.servlet.MockRequest;
import org.apache.click.util.HtmlStringBuffer;
import org.apache.commons.lang3.StringUtils;

/**
 * Test Html5Form behavior.
 */
public class Html5FormTest extends TestCase {

    private Form testForm;
    private HiddenField trackField;

    /**
     * Inicializa o cenário de teste mapeando o campo interno de nome do
     * formulário. Segue a risca o método setUp original do framework.
     */
    @Override
    public void setUp() {
        // Inicializa o componente sob teste usando a nova classe HTML5
        testForm = new Html5Form("form");

        // O Click cria automaticamente um HiddenField para gerenciar o nome do formulário
        trackField = (HiddenField) testForm.getField(Form.FORM_NAME);

        assertTrue(testForm.getControls().indexOf(trackField) == 0);
        assertTrue(testForm.getFieldList().indexOf(trackField) == 0);
    }

    /**
     * Teste refinado baseado no caso CLK-267. Garante que a lógica de prevenção
     * de submissão duplicada (Tokens de Sessão) funciona perfeitamente no
     * Html5Form e não injeta tabelas ao renderizar tokens.
     */
    public void testDuplicateOnSubmitCheck() {
        MockContext context = MockContext.initContext("test-form.htm");
        MockRequest request = context.getMockRequest();
        request.setParameter("form_name", "form");

        Page page = new Page();
        page.setStateful(true);

        Form form = new Html5Form("form");
        String submitCheckName = Form.SUBMIT_CHECK + form.getName() + "_" + context.getResourcePath();

        // Primeiro submit check
        boolean valid = form.onSubmitCheck(page, "/invalid-submit.html");
        Assert.assertTrue(valid);

        Field submitCheckField = form.getField(submitCheckName);
        Assert.assertNotNull(submitCheckField);

        request.setParameter(Form.SUBMIT_CHECK + form.getName() + "_" + context.getResourcePath(),
                submitCheckField.getValue());

        // Segundo submit check (deve continuar válido)
        valid = form.onSubmitCheck(page, "/invalid-submit.html");
        Assert.assertTrue(valid);

        // REFINAMENTO HTML5: Garante que os campos de token ocultos se renderizam sem quebrar a árvore de DIVs
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();

        Assert.assertFalse("O formulário com tokens de segurança não deve conter tabelas", htmlResult.contains("<table"));
        Assert.assertTrue("Deve manter a abertura em DIV semântica", htmlResult.contains("<div class=\"click-html5-form\""));
    }

    /**
     * Teste refinado baseado no caso CLK-289. Garante o bloqueio de requisições
     * maliciosas sem gerar tabelas em telas de erro.
     */
    public void testOnSubmitCheckMissingParam() {
        MockContext context = MockContext.initContext("test-form.htm");
        MockRequest request = context.getMockRequest();
        request.setParameter("form_name", "form");

        Page page = new Page();
        Form form = new Html5Form("form");
        String submitTokenName = Form.SUBMIT_CHECK + form.getName() + "_" + context.getResourcePath();

        Field submitCheckField = form.getField(submitTokenName);
        Assert.assertNull(submitCheckField);

        boolean valid = form.onSubmitCheck(page, "/invalid-submit.html");
        Assert.assertTrue(valid);

        submitCheckField = form.getField(submitTokenName);
        request.setParameter(submitTokenName, submitCheckField.getValue());

        valid = form.onSubmitCheck(page, "/invalid-submit.html");
        Assert.assertTrue(valid);

        // Simula a remoção do token por manipulação externa (Hacker check)
        request.removeParameter(submitTokenName);
        valid = form.onSubmitCheck(page, "/invalid-submit.html");

        // A submissão deve ser considerada inválida
        Assert.assertFalse(valid);
    }

    /**
     * Garante que o binding automático de parâmetros da requisição para o
     * objeto Java permanece intocado e transparente na nova classe.
     */
    public void testFormOnProcessRequestBinding() {
        MockContext context = MockContext.initContext("test-form.htm");
        MockRequest request = context.getMockRequest();
        String requestValue = "one";

        request.setParameter("form_name", "form");
        request.setParameter("name", requestValue);

        Form form = new Html5Form("form");
        TextField nameField = new TextField("name");
        form.add(nameField);

        Assert.assertNull(nameField.getValueObject());

        // Executa o processamento padrão do Click
        form.onProcess();

        // O valor capturado no request deve estar vinculado ao componente Java
        Assert.assertEquals(requestValue, nameField.getValueObject());
    }

    /**
     * Teste refinado baseado no caso CLK-666. Garante que a substituição de
     * campos duplicados em tempo de execução mantém os índices limpos na nova
     * árvore de renderização vertical.
     */
    public void testReplaceFields() {
        // CORREÇÃO: Inicializa o contexto para que o método .render() 
        // encontre um request simulado no ThreadLocal Stack!
        MockContext.initContext();
        Form form = new Html5Form("form");
        Field child1 = new TextField("child1");
        Field child2 = new TextField("child2");
        form.add(child1);
        form.add(child2);

        assertEquals(3, form.getControlMap().size());
        assertEquals(3, form.getControls().size());

        // Substitui adicionando instâncias com o mesmo nome
        child1 = new TextField("child1");
        child2 = new TextField("child2");
        form.add(child1);
        form.add(child2);

        assertEquals(3, form.getControlMap().size());
        assertSame(child1, form.getControls().get(0));
        assertSame(child2, form.getControls().get(1));

        // REFINAMENTO HTML5: Certifica-se de que a substituição dinâmica de nós não corrompeu a saída semântica
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();

        assertEquals("Não deve conter tags de tabela após substituição de campos", 0, StringUtils.countMatches(htmlResult, "<tr"));
        assertTrue("Deve conter a estrutura em bloco para os novos campos substituídos", htmlResult.contains("class=\"form-field-group\""));
    }

    /**
     * Teste refinado baseado no caso CLK-715. Garante a integridade e
     * isolamento de memória das APIs de gerenciamento de estado (Session State)
     * essenciais para páginas complexas do Click.
     */
    public void testGetAndSetState() {
        MockContext.initContext();
        Form form = new Html5Form("form");

        Field nameField = new TextField("name");
        Field ageField = new TextField("age");
        nameField.setValue("Steve");
        ageField.setValue("10");
        form.add(nameField);
        form.add(ageField);

        // Captura o mapa de estados da sessão
        Object state = form.getState();
        Map<?, ?> formStateMap = (Map<?, ?>) state;

        assertEquals(formStateMap.get(nameField.getName()), nameField.getValue());
        assertEquals(formStateMap.get(ageField.getName()), ageField.getValue());

        // Simula a restauração do estado em um formulário zerado
        Form blankForm = new Html5Form("form");
        Field blankNameField = new TextField("name");
        Field blankAgeField = new TextField("age");
        blankForm.add(blankNameField);
        blankForm.add(blankAgeField);

        blankForm.setState(formStateMap);

        // Verifica se os valores foram restaurados perfeitamente
        assertEquals("Steve", blankNameField.getValue());
        assertEquals("10", blankAgeField.getValue());
    }
}
