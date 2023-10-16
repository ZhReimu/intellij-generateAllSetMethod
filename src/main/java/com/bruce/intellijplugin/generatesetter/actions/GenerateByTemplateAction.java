/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package com.bruce.intellijplugin.generatesetter.actions;

import com.bruce.intellijplugin.generatesetter.CommonConstants;
import com.bruce.intellijplugin.generatesetter.template.GenerateSetterService;
import com.bruce.intellijplugin.generatesetter.template.GenerateSetterState;
import com.bruce.intellijplugin.generatesetter.template.Template;
import com.bruce.intellijplugin.generatesetter.template.TemplateDialog;
import com.bruce.intellijplugin.generatesetter.utils.PsiDocumentUtils;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author bruce ge 2022/9/13
 */
public class GenerateByTemplateAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiClass localVarialbeContainingClass = GenerateAllSetterAction.getLocalVarialbeContainingClass(element);
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        String name = psiLocal.getName();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                GenerateSetterState state = GenerateSetterService.getInstance().getState();
                TemplateDialog templateDialog = new TemplateDialog(element.getProject(), true, state.getTemplateList(), localVarialbeContainingClass, name);
                boolean b = templateDialog.showAndGet();
                if (b) {
                    String generatedText = templateDialog.getGeneratedText();
                    PsiElement parent1 = psiLocal.getParent();
                    PsiDocumentManager psiDocumentManager = PsiDocumentManager
                            .getInstance(project);
                    PsiFile containingFile = element.getContainingFile();
                    WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                        @Override
                        public void run() {
                            Document document = psiDocumentManager.getDocument(containingFile);
                            document.insertString(parent1.getTextOffset() + parent1.getText().length(),
                                    generatedText);
                            PsiDocumentUtils.commitAndSaveDocument(psiDocumentManager, document);
                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiClass localVarialbeContainingClass = GenerateAllSetterAction.getLocalVarialbeContainingClass(element);
        if (localVarialbeContainingClass == null) {
            return false;
        }
        GenerateSetterState state = GenerateSetterService.getInstance().getState();
        Boolean generateByTemplate = state.getGenerateByTemplate();
        List<Template> templateList = state.getTemplateList();
        return generateByTemplate && CollectionUtils.isNotEmpty(templateList);
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return CommonConstants.GENERATE_SETTER_METHOD;
    }

    @NotNull
    @Override
    public String getText() {
        return "Generate By Template";
    }
}
