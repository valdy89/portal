import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AlertService } from '../_services/index';

@Component({
    moduleId: module.id,
    templateUrl: 'services.component.html'
})

export class ServicesComponent { 

    model: any = {};

    constructor(
        private router: Router,
        private alertService: AlertService) { }



}
