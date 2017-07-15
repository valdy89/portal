import { Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import { Router } from '@angular/router';

import { Tenant } from '../_models/index';
import { AlertService, TenantService } from '../_services/index';



@Component({
    selector: 'tenant-edit-form',
    templateUrl: 'tenantForm.component.html'
})

export class TenantFormComponent  implements OnInit{


  @Output() onSave = new EventEmitter<any>();
  @Output() onCancel = new EventEmitter<any>();
  @Input() tenant: Tenant;
  @Input() action: string = 'create';
  //selectedTenant: Tenant = new Tenant("eso","eso"","","","","","","",""",true, 15,25,"","",5);

  ngOnInit (){
    console.log(this.tenant);
    this.tenant = this.tenant;
  }

  save(){
    console.log(this.tenant);
    this.onSave.emit(true);
  }

  cancel(){
    this.onCancel.emit(false);
  }

}
