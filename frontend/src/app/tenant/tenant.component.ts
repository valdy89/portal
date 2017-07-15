import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';

import { Tenant } from '../_models/index';
import { AlertService, TenantService } from '../_services/index';
 import { TenantFormComponent } from '../tenant/index';


@Component({
    moduleId: module.id,
    templateUrl: 'tenant.component.html'
})

export class TenantComponent implements  OnInit  {

  tenants: Tenant[] = [];
  selectedTenant: Tenant;
  cancelCopy: Tenant;
  action: string;
  showform: boolean = false;




  testTenant: Tenant = new Tenant("eso","eso",false, 15,25,"","",15);
  testTenant2: Tenant = new Tenant("eso","eso",false, 15,25,"","",15);




  constructor(private tenantService: TenantService,
              private alertService: AlertService,
              private translateService: TranslateService){
                // let text = this.translateService.getTranslation('TENANT.ADD');
                  let text = this.translateService.get('TENANT.ADD', {value: 'world'}).subscribe((res: string) => {
    console.log(res);
    //=> 'hello world'
});
                console.log(text);
  };


  ngOnInit (){
  //  this.loadAllTenants();
  console.log(this.testTenant);
  console.log(this.tenants);

    this.tenants.push(this.testTenant);
    this.tenants.push(this.testTenant2);

  }


  private loadAllTenants() {
      this.tenantService.getTenants().subscribe(data => {
        this.tenants = data;
        console.log(this.tenants)


      });

  }

  private selectTenant(tenant : Tenant, action: string){
      this.selectedTenant = tenant;
      this.action = action;
      this.cancelCopy = JSON.parse(JSON.stringify(tenant));
      this.showform = true;

  }


  private createTenant(){
      this.selectedTenant = new Tenant("","",false, 0,0,"","",0);
      this.action = 'create';
      this.showform = true;
  }


  private deleteTenant(tenant){
    this.tenantService.delete(tenant).subscribe(
          error => {
              this.alertService.error(error);
          });
    this.showform = false;
  }



  onSave(){
    console.log(this.action);
    switch (this.action) {
      case 'create':

        this.tenantService.create(this.selectedTenant);
        this.alertService.success(this.translateService.getTranslation('TENANT.ADD') + 'Vytvoření nového tenantu bylo úspěšné', false);
        //Statements executed when the result of expression matches value1
      break;

      case 'update':

        this.tenantService.update(this.selectedTenant);
        this.alertService.success('Zmena tenantu byla úspěšná', false);
        //Statements executed when the result of expression matches value1
      break;
    default:
      this.alertService.error('Nepovolená operace nad dokumenty', false);
    //Statements executed when none of the values match the value of the expression
    break;
}
    this.showform = false;

  }


  onCancel(){
    this.selectedTenant = this.cancelCopy;
    this.cancelCopy = null;
    this.showform = false;
  }
}
