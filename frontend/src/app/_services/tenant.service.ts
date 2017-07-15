import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions, Response } from '@angular/http';

import { Tenant } from '../_models/index';

@Injectable()
export class TenantService {
    constructor(private http: Http) {
     }

    getTenants() {
       return this.http.post('http://192.168.0.102:8080/tenant', '', this.jwt()).map((response: Response) => response.json());
    }
    getById(id: number) {
        return this.http.get('/tenant/' + id, this.jwt()).map((response: Response) => response.json());
    }

    create(tenant: Tenant){
      return this.http.put('http://192.168.0.102:8080/tenant/', JSON.stringify(tenant), this.jwt()).map((response: Response) => response.json());
    }

    update(tenant: Tenant){
      return this.http.post('http://192.168.0.102:8080/tenant', JSON.stringify(tenant), this.jwt()).map((response: Response) => response.json());
    }

    delete(tenant: Tenant){
      return this.http.post('http://192.168.0.102:8080/tenant', JSON.stringify(tenant), this.jwt()).map((response: Response) => response.json());
    }




    // private helper methods

    private jwt() {
        // create authorization header with jwt token

            let headers = new Headers();
            return new RequestOptions({ headers: headers, withCredentials: true });
    }
}
