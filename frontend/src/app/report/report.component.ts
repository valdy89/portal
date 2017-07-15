import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AlertService } from '../_services/index';

@Component({
    moduleId: module.id,
    templateUrl: 'report.component.html'
})

export class ReportComponent {

    model: any = {};

    constructor(
        private router: Router,
        private alertService: AlertService) { }



}
