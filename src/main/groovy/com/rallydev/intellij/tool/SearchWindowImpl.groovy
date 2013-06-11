package com.rallydev.intellij.tool

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

//todo: store checkbox state
class SearchWindowImpl extends RallyToolWindow implements ToolWindowFactory {

    ToolWindow myToolWindow
    Map<String, Artifact> searchResults

    public void createToolWindowContent(com.intellij.openapi.project.Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow
        Content content = getContentFactory().createContent(myToolWindowContent, "", false)
        setupWindow()
        toolWindow.getContentManager().addContent(content)
    }

    void setupWindow() {
        setupTypeChoices()
        setupProjectChoices()
        setupTable()
        installSearchListener()
    }

    //todo: for mocking - explore IntelliJ's base test cases for different way
    ContentFactory getContentFactory() {
        ContentFactory.SERVICE.getInstance()
    }

    void setupTypeChoices() {
        //todo: future - dynamically setup choices from typedefs
        typeChoices.setModel(new DefaultComboBoxModel(
                ['', 'Defect', 'Requirement'].toArray()
        ))
    }

    void setupProjectChoices() {
        projectChoices.addItem(new ProjectItem(project: new Project(name: '')))
        ServiceManager.getService(ProjectCacheService.class).cachedProjects.each {
            projectChoices.addItem(new ProjectItem(project: it))
        }
    }

    void setupTable() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            boolean isCellEditable(int row, int column) {
                false
            }
        }
        resultsTable.model = model

        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.clickCount == 2) {
                    println searchResults[(String)resultsTable.getValueAt(resultsTable.selectedRow, 0)]
                }
            }
        })

        model.addColumn('Formatted ID')
        model.addColumn('Name')
        model.addColumn('Description')
        model.addColumn('Type')
        model.addColumn('Project')
    }

    void installSearchListener() {
        searchButton.addActionListener(
                new SearchListener(
                        window: this, tableModel: (DefaultTableModel) resultsTable.getModel()
                )
        )
        // ?. for test - explore IntelliJ test framework to better handle
        searchPane.rootPane?.setDefaultButton(searchButton)
    }

    Class getSelectedType() {
        switch (typeChoices.selectedItem) {
            case 'Defect':
                return Defect
            case 'Requirement':
                return Requirement
            default:
                return Artifact
        }
    }

    List<String> getSearchAttributes() {
        List<String> searchAttributes = []
        if (formattedIDCheckBox.selected) {
            searchAttributes << 'FormattedID'
        }
        if (nameCheckBox.selected) {
            searchAttributes << 'Name'
        }
        if (descriptionCheckBox.selected) {
            searchAttributes << 'Description'
        }
        searchAttributes
    }

    String getSearchTerm() {
        searchBox.text
    }

    String getSelectedProject() {
        projectChoices.getSelectedItem()?.project?._ref
    }

    static class ProjectItem {
        @Delegate
        Project project

        @Override
        String toString() {
            project.name
        }
    }

}
