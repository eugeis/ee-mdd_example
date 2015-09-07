
model ('MddExample', key: 'cg', namespace: 'ee.mdd', uri: 'cg.test') {

  component('MddExample', key: 'me', namespace: 'example', artifact: 'ee-mdd_example') {

        module('shared') {

      enumType('TaskType', defaultLiteral: 'Unknown', desc: '''Defines the type of a task''') {
        prop('code', type: 'Integer')

        constr { param(prop: 'code') }

        lit('Unknown', body: '-1')
        lit('Open', body: '1')
        lit('Closed', body: '2')
      }

      enumType('AreaLocation',
      description: '''Definition where an area is located see chapter 347''') {
        prop('code', type: 'Integer')
        
        constr { param(prop: 'code') }
        constr {}
      
        lit('BothSides', body: '-13')
        lit('LeftSide', body: '23')
        lit('OnTrack')
        lit('RightSide', body: '2338')
       
      }
      
      enumType('CatenaryRestrictionState') {
        lit('New')
        lit('Planned')
        lit('ActivationRequested')
        lit('Activated')
        lit('ActivationAborted')
        lit('RevocationRequested')
        lit('RevocationIncomplete')
        lit('Ended')
  
        op('isDeletable', ret: 'boolean', override: true,
          description: 'Used to indicate when the catenary restriction is allowed to be deleted.',
          body: '''return (this == PLANNED) || (this == ENDED);''')

      op('isModifiable', ret: 'boolean', override: true,
        description: 'Used to indicate when the catenary restriction is allowed to be modified.',
        body: '''return (this == PLANNED);''')
  
        op('isSignalmanAttentionRequired', ret: 'boolean', override: true,
          description: 'Used to indicate when catenary restriction is in a state that requires signalman attention',
          body: '''return (this == ACTIVATION_ABORTED);''')
    }

      container('TaskContainer', base:true) {
        prop('Signal', type: 'Signal', cache: true)
        prop('ProtectionRequirement', type: 'ProtectionRequirement')
        prop('Trophy', type: 'Trophy')

        controller(cache: true) {}
      }
      
      
      entity('CatenaryRestriction', idGeneratorName: 'RM_RSTRCTN_SEQ', clientCache: true,
        description: 'CatenaryRestriction represents an area of railroad tracks and devices where power is shut off.') {
          prop('id', type: 'Long', primaryKey: true, unique: true)
          prop('state', type: 'CatenaryRestrictionState', defaultValue: 'CatenaryRestrictionState.NEW')
          prop('previousState', type: 'CatenaryRestrictionState')
          prop('stateTimeout', type: 'Date')
          prop('reasonForStateChange', type: 'String', description: 'Contains the reason for the last state change.')
          prop('topologyIds', type: 'Long', multi:true, sqlName:'ELMNTS', description: 'The list of catenary areas that are affected by the catenary restriction')
          op('getRestrictionType', ret: 'RestrictionType', override: true, body: '''return RestrictionType.CATENARY_RESTRICTION;''')
          op('cloneCatenaryRestriction', ret: 'CatenaryRestriction', body: '''CatenaryRestriction clone = ObjectUtils.clone(this); return clone;''')

      commands(base:true) {
        update(fireEventProp:true) { param(prop: 'state') }
      }
      
      finder(base:true) {
        findBy { param(prop: 'stateTimeout') }
        findBy { param(prop: 'state') }
      }
    }

      
      
      
      

      entity('Trophy') {
        prop('id', type: 'Long', unique: true, primaryKey: true)
        prop('value', type: 'Integer')
      }
      
      



      entity('ProtectionRequirement', superUnit: 'ElementLink', sqlName: 'PR',
      description: '''Protection requirements are element references which extend the reference by an element specific value They are used for possession areas to specify eg the locked position of a switch They can be specified in an ElementRefs collection instead of the ElementRef tag''') {
        prop('protectionKey', type: 'ProtectionKeyType', description: '''The key of the attribute which must have for the referenced element a particular value''')
        prop('protectionValue', type: 'ProtectionValueType', description: '''This is a global text field which can be used by the several protection requirements to specify restrictions Eg the locked position of a switch''')
      }

      enumType('ProtectionKeyType',
      description: '''Possible keys for a Protection Requirement property''') {
        lit('BlockState')
        lit('SwitchPosition')
      }

      enumType('ProtectionValueType',
      description: '''Possible values for a Protection Requirement property''') {
        lit('Blocked')
        lit('Left')
        lit('Right')
        lit('Undef')
      }

      entity('ElementLink', virtual: true, base: true, description: '''A reference to an Element object''') {
        prop('id', type: 'Long', unique: true, primaryKey: true, xml: false, hashCode: true)
        prop('desc', type: 'String', sqlName: 'DSC', description: '''This is a description text which identifies the referenced element by a readable name Used only to make the XML file readable''')
        prop('topologyId', type: 'Long', sqlName: 'T_ID', hashCode: true, index: true, description: '''The topology Id of the referenced Element''')
      }

      basicType('Coordinate', base: true,
      description: '''The coordinates of the item in the internal planning tool for the topography''') {
        prop('x', type: 'Long', description: '''The xvalue of this location''')
        prop('y', type: 'Long', description: '''The yvalue of this location ''')
      }

      entity('Signal', base: true) {
        prop('id', type: 'Long', primaryKey: true, unique: true)
        prop('state', type: 'Integer')
        prop('position', type: 'Long')

        op('testOperation') {
          param('size', type: 'Integer')
        }
      }

      entity('Element', virtual: true,
      description: '''An element can be any general topological item which can be identified by a a topological Id and a name An Element can be assigned a ControlArea''') {
        prop('id', type: 'Long', unique: true, primaryKey: true, xml: false, hashCode: true)
        prop('longName', type: 'String', index: true, description: '''Long name of the element''')
        prop('shortName', type: 'String', hashCode: true, index: true, description: '''Short name of the element''')
        prop('topologyId', type: 'Long', sqlName: 'T_ID', hashCode: true, index: true, description: '''Unique Id assigned by engineering''')
        prop('type', type: 'Element', description: '''The type classification of the Element''')

        commands {
          delete { param(prop: 'topologyId') }
        }

        finder {
          exist {  param(prop: 'shortName')  }
          exist {  param(prop: 'topologyId')  }
          findBy {  param(prop: 'shortName')  }
          findBy {  param(prop: 'topologyId') }
        }
      }

      entity('AllowedConnection',
      description: '''Describes an allowed connection by type which can be attached in the timetable to this location''') {
        prop('id', type: "Long", unique: true, primaryKey: true, xml: false, hashCode: true)
        prop('direction', type: 'Integer', index: true, description: '''The direction in which the connection is allowed''')
        prop('stationTrack', type: 'StationTrack', opposite: 'allowedConnections')
        prop('type', type: 'String', description: '''The type of the connection''')
      }

      entity('StationTrack', superUnit: 'Element',
      description: '''A station track is a track belonging to a station The name of the station track is often shown in passenger timetables<br/><br/>A station track without platform track can only be used for technical stops ''') {
        prop('id', type: 'Long', unique: true, primaryKey: true)
        prop('allowedConnections', type: 'AllowedConnection', multi: true, opposite: 'stationTrack', description: '''The list of allowed connections''')
        prop('isVirtual', type: 'Boolean', description: '''Marks this station track as virtual<br/><br/>Such a station track can be used to express relations from routes to stations on this route''')
        prop('position', type: 'Integer', description: '''The position of this station track''')
        prop('tgmtNumber', type: 'Integer', description: '''The platform number used in TGMT''')
      }

      entity('Comment', attributeChangeFlag: true) {
        prop('id', type: 'Long',  unique: true, primaryKey: true)
        prop('testTask', type: 'Task', opposite: 'comment')
        prop('testProp', type: 'Task', multi: true)
        prop('dateOfCreation', type: 'Date')
        prop('newTask', type: 'Task')

        constr {}

        commands {
          delete { param(prop: 'dateOfCreation') }
        }
      }

      entity('Task', attributeChangeFlag: true, ordered: true) {
        prop('id', type: 'Long', primaryKey: true, unique: true)
        prop('comment', type: 'Comment', opposite: 'testTask')
        prop('created', type: 'Date', unique: true)
        prop('closed', type: 'Date', index: true)
        prop('actions', type: 'String' )
        prop('size', type: 'Integer')
        prop('order', type: 'Long', xml: 'false')

        constr {}

        constr {
          param(prop: 'id')
          param(prop: 'created', value: '#newDate')
        }

        constr {
          param(prop: 'actions')
          param(prop: 'created')
          param(prop: 'closed')
        }

        op('hello', body: '#testBody') {
          param('Test', type: 'String')
          param('countdown', type: 'Integer')
        }

        index( props: [
          'comment',
          'created'
        ])

        finder {
          findBy { param(prop: 'comment' ) }
          count { param(prop: 'created') }
          exist {
            param(prop: 'created')
            param(prop: 'closed')
          }
        }

        commands {
          delete { param(prop: 'closed') }
        }
      }

      entity('TaskAction', description: '''The entity that represents the action''') {
        prop('id', type:'Long', unique:true, primaryKey:true)
        prop('task', type:'Task', opposite:'actions', description: '')
        prop('name', type:'String')
      }

      channel('NotificationTopic') {
        message(ref: 'TaskContainer')
        message(ref: 'TaskAction')
        message(ref: 'Task')
      }
    }

    module('backend') {

      entity('Area') {
        prop('id', type: 'Long', unique: true, primaryKey: true)
        prop('name', type: 'String')
        prop('age', type: 'Integer')
        prop('size', type: 'Integer')

        commands {
          delete() { param(prop: 'name') }
        }

        finder {
          exist  {  param(prop: 'name') }
          findBy {  param(prop: 'name') }
          findBy {  param(prop: 'size') }
        }
      }

      controller('TaskAgregator', base: true) {
        op('hello', ret: 'String', body: '#testBody') {
          param('test', type: 'String')
        }
      }

      facade('CommandService', base: true) {
        delegate(ref: 'TaskAgregator.hello')
        delegate(ref: 'Area.commands.deleteByName')
        delegate(ref: 'Area.finder.findByName')
        delegate(ref: 'Area.finder.findBySize')
        delegate(ref: 'Area.finder.existByName')
      }
      
      stateMachine('CatenaryRestrictionWorkflow', key: 'catworkflow', entityRef: '//shared.CatenaryRestriction', statePropRef: 'state', stateTimeoutPropRef: 'stateTimeout',
        timeoutCheckInterval: '2s', description: 'State machine for Catenary Restrictions', generatePermissionsForEvents: true) {
      
          controller('CatenaryRestrictionController', superUnit:'//backend.RestrictionWorkflowController', description: 'The controller provides operations for Catenary Restrictions',
          types:[
            'com.siemens.ra.cg.ats.disp.rm.model.CatenaryRestriction'
          ]) {
      
            op('deleteCatenaryRestrictions',
            description: 'Deletes all Catenary Restrictions',
            body: '''catenaryRestrictionManager.deleteAll();''')

      op('updateCatenaryRestriction', ret: 'CatenaryRestriction') {
        param('restriction', type: 'CatenaryRestriction')
      }
      // delegates could not be resolved
      //delegate(ref:'CatenaryRestriction.commands.create')
      //delegate(ref:'shared.CatenaryRestriction.commands.findByState')

      //inject('//backend.CatenaryRestrictionHistoryEntry.manager')
    }

    //actions
    action('requestCatenaryActivation', async: true)
    action('requestCatenaryRevocation', async: true)
    action('updateReasonForStateChange')
    action('resetReasonForStateChange', body: 'context.getCatenaryRestriction().setReasonForStateChange(null);')
    action('setActualStartDate', body: 'context.getCatenaryRestriction().setActualStartDate(new java.util.Date());')
    action('setActualEndDate', body: 'context.getCatenaryRestriction().setActualEndDate(new java.util.Date());')

    //conditions
    condition('userIsAssignedToCatenaryRestriction')

    //events
    event('create')
    event('modify')
    event('activateReq')
    event('activateConfirm')
    event('activateError', alternative: true) { prop('reason', type: 'String') }
    event('revokeReq')
    event('revokeConfirm')
    event('revokeError', alternative: true) { prop('reason', type: 'String') }
    event('revokeManually')
    event('reset')

    //states
    state('New') {
      on('create', to: 'Planned', fireEvent: false, groups:['Signalman', 'Planner', 'OmCoordinator'])
    }

    state('Planned') {
      on('activateReq', to: 'ActivationRequested', conditions:['userIsAssignedToCatenaryRestriction'],
        fireEvent: true, groups:['Signalman', 'OmCoordinator'])
    }

    state('ActivationRequested', timeout: '1min', entryActions: ['requestCatenaryActivation']) {
      on('activateConfirm', to: 'Activated', actions: ['resetReasonForStateChange'],
        fireEvent: false, groups:['Tms'])
      on('activateError', to: 'ActivationAborted', actions: ['updateReasonForStateChange'],
        fireEvent: false, groups:['Tms'])
      on('timeout', to: 'ActivationAborted')
    }

    state('Activated', entryActions: ['setActualStartDate']) {
      on('revokeReq', to: 'RevocationRequested', conditions:['userIsAssignedToCatenaryRestriction'],
        fireEvent: true, groups:['Signalman','OmCoordinator'])
    }

    state('ActivationAborted') {
      on('reset', to: 'Planned', actions: ['resetReasonForStateChange'],
         conditions:['userIsAssignedToCatenaryRestriction'],
         fireEvent: false, groups:['Signalman','Planner'])
    }

    state('RevocationRequested', timeout: '1min', entryActions: ['requestCatenaryRevocation']) {
      on('revokeConfirm', to: 'Ended', actions: ['resetReasonForStateChange'], groups:['Tms'])
      on('revokeError', to: 'RevocationIncomplete', actions: ['updateReasonForStateChange'],
        fireEvent: false, groups:['Tms'])
      on('timeout', to: 'RevocationIncomplete')
    }

    state('RevocationIncomplete') {
      on('revokeManually', to: 'Ended', actions: ['resetReasonForStateChange'], conditions:['userIsAssignedToCatenaryRestriction'],
        fireEvent: true, groups:['Signalman','OmCoordinator'])
    }

    state('Ended', entryActions: ['setActualEndDate']) {

    }

    history('//backend.CatenaryRestrictionHistoryEntry', oldStateProp: 'previousState', newStateProp: 'newState',
    actorProp: 'actor', actionProp: 'action', dateProp: 'dateOfOccurrence', reasonProp: 'reasonForStateChange', stateMachineEntityHistoryEntriesProp: 'historyEntries')

    stateMachineController {}

    context {}
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
      
    }

    module('test', namespace: 'test') {
    }

    module('cli', namespace: 'cli') {
    }

    module('backend_jpa', namespace: 'jpa') {
    }
 
  }
}