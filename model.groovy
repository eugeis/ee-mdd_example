
model ('MddExample', key: 'cg', namespace: 'ee.mdd', uri: 'cg.test') {

  component('MddExample', key: 'me', namespace: 'example', artifact: 'ee-mdd_example') {

    module('shared') {
      enumType('TaskType', description: '''Defines the type of a task''') {
        lit('Info')
        lit('Todo')
      }
    }

    module('backend') {
      entity('ExampleEntity', virtual: true, base: true, description: '''The base entity for all entities for the example component.''') {
        prop('id', type:'Long', unique:true, primaryKey:true, hashCode: true)
        prop('name', type: 'String', hashCode: true, index: true)
      }

    entity('Task', superUnit: 'ExampleEntity', labelBody: 'String.format("%s(%s)", name, id)', description: '''A task describes an job''') {
      prop('type', type: 'TaskType', defaultValue:'TaskType.TODO')
      prop('comment', type: 'String')
      prop('actions', type:'TaskAction', opposite: 'task', multi:true)

      commands {
        create {
//        param(prop: 'name') name will not be found because it is not defined in 'Task'
          param(prop: 'type')
          param(prop: 'comment')
        }
//      update { param(prop: 'name') }
        update { param(prop: 'comment') }
      }
      
      finder {
//      findBy {  param(prop: 'name')  }
        findBy {  param(prop: 'type')  }
      }
    }

    entity('TaskAction', superUnit: 'ExampleEntity', description: '''The entity that represents the action''') {
      prop('task', type:'Task', opposite:'actions', description: '')
      
      commands {}
      
      finder {}
    }

    entity('TaskFilter', superUnit: 'ExampleEntity', labelBody: 'String.format("%s(%s)", name, id)', description: '''A task describes an job''') {
      prop('nameQuery', type: 'String')
      prop('commentQuery', type: 'String')

      commands {}
      
      finder{}
    }

    container('TaskContainer', description: '''Container for tasks and all dependend objects''') {
      prop('task', type: 'Task')
      prop('taskAction', type: 'TaskAction')

      controller(cache: true, importChanges: true) { }
    }

    facade('ExampleQueryService', description:'''The Example Query Service provides functionality to retrieve tasks information.'''){

//      delegate(ref: '//backend.TaskContainer.controller.loadAll', name: 'findTaskContainer')
//      delegate(ref: 'TaskContainer.controller.loadVersions')
//      delegate(ref: 'TaskContainer.controller.loadDiff')
//      delegate(ref: 'Task.finder.findAll')
      delegate(ref: 'Task.finder.findByType')
//      delegate(ref: 'Task.finder.findById')
    }

    facade('ExampleCommandService', description:'''The Example Command Service provides functionality to manage tasks.'''){

      op('createTask') {
        param('task', type: 'Task')
        param('waitMillisAfterCreation', type: 'long')
      }

//      delegate(ref: 'TaskContainer.controller.importChangesContainer')
//      delegate(ref: 'Task.commands.create')
        delegate(ref: 'Task.commands.createByTypeAndComment')
//        delegate(ref: 'Task.commands.update')
    }
      
    facade('ExampleAdminService'){
      op('reset')
//      inject(ref: 'Task.manager')
//      inject(ref: 'TaskAction.manager')
    }
  }

    module('cfg') {
    }

    module('api') {
    }

    module('client') {
    }

    module('ui') {
      
      
      view ('TaskEditor', main:true) {
        presenter {}
        viewModel {}
        button('accept') { onAction(['TaskEditorView.model']) }
        button('discard') { onAction(['TaskEditorView.model']) }
        dialog{}
      }
  
      view ('TaskExplorer') {
        button('addTask') { onAction(['TaskEditorView.model']) }
        button('deleteTask') { onAction(['TaskEditorView.model']) }
        table('tasks') { onSelect(['TaskEditorView.model'], observerRefs:['TaskDetailsView.presenter']) }
        presenter {}
      }
  
      view ('TaskDetails') {
        textField('taskName') { onChange(['TaskEditorView.model']) }
        table('actions') {onSelect()}
        contextMenu('actionsManagement') {}
        presenter {}
      }
  
      view ('TaskSearch') {
        textField('name') { onChange() }
        textField('comment') { onChange() }
        button('search') { onAction() }
        presenter {}
        dialog {}
      }
      
    }

    module('test', namespace: 'test') {
    }

    module('cli', namespace: 'cli') {
    }

    module('backend_jpa', namespace: 'jpa') {
    }
 
  }
}