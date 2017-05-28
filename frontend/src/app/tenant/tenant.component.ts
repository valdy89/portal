import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Tenant } from '../_models/index';
import { AlertService, TenantService } from '../_services/index';



@Component({
    moduleId: module.id,
    templateUrl: 'tenant.component.html'
})

export class TenantComponent implements  OnInit  {
  data: any = [];
  tenants: Tenant[];

  constructor(private tenantService: TenantService){  };

  ngOnInit (){
    this.loadAllTenants();
  }


  private loadAllTenants() {
      this.tenantService.getTenants().subscribe(data => { this.tenants = data;   console.log(this.tenants)});
      //console.log(this.data);
  }
}
