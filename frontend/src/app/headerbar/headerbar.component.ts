import { Component } from '@angular/core';

import {TranslateService} from '@ngx-translate/core';

import { User } from '../_models/index';
import { UserService } from '../_services/index';

@Component({
    moduleId: module.id,
    selector: 'headerbar',
    templateUrl: 'headerbar.component.html'
})

export class HeaderbarComponent {
  currentUser: User;

  constructor(private translate: TranslateService, private userService: UserService) {
        this.currentUser = JSON.parse(localStorage.getItem('currentUser'));

    }
}
