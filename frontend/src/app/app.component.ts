import { Component } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Component({
    moduleId: module.id,
    selector: 'app-root',
    templateUrl: 'app.component.html'
})

export class AppComponent { constructor(private translate: TranslateService) {
        translate.addLangs(["en", "cz"]);
        translate.setDefaultLang('en');

        let browserLang = translate.getBrowserLang();
        translate.use(browserLang.match(/en|cz/) ? browserLang : 'en');
    }}
