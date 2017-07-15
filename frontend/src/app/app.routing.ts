import { Routes, RouterModule } from '@angular/router';

import { HomeComponent } from './home/index';
import { LoginComponent } from './login/index';

import { ReportComponent } from './report/index';

import { ServicesComponent } from './services/index'; 
import { RegisterComponent } from './register/index';
import { TenantComponent , TenantFormComponent} from './tenant/index';

import { AuthGuard } from './_guards/index';

const appRoutes: Routes = [
    { path: '', component: HomeComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'tenant', component: TenantComponent },
    { path: 'report', component: ReportComponent },
    { path: 'services', component: ServicesComponent },
    // otherwise redirect to home
    { path: '**', redirectTo: 'login' }
];

export const routing = RouterModule.forRoot(appRoutes);
