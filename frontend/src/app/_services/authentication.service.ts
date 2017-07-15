import { Injectable } from '@angular/core';
import { Http, Headers, Response, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';


@Injectable()
export class AuthenticationService {
    constructor(private http: Http) { }

    login(username: string, password: string) {

      let headers: Headers = new Headers();
      headers.append("Authorization", "Basic " + btoa(username + ":" + password));
      headers.append("Content-Type", "application/x-www-form-urlencoded");

      //this.http.get
      // let res = this.http.post('http://192.168.0.102:8080/login', void 0, headers)
      //     .map((response: Response) => {
      //         // login successful if there's a jwt token in the response
      //     //    let cookieToken = response.headers;
      //
      //         let headers: Headers = response.headers;
      //         console.log(headers);
      //         headers.getAll('set-cookie');
      //         let user = response.json();
      //     //    localStorage.setItem('setCookie', res.headers._headers.get("Set-Cookie")[0]);
      //
      //         //user.token = cookieToken;
      //         if (user) {
      //             // store user details and jwt token in local storage to keep user logged in between page refreshes
      //             localStorage.setItem('currentUser', JSON.stringify(user));
      //           //  localStorage.setItem();
      //         }
      //     });
      //
      //
      //     let params;
      return this.http.get('http://192.168.0.102:8080/login', {headers: headers})
          .map((response: Response) => {
              let user = response.json();
              console.log(response);
              if (user) {
                  // store user details and jwt token in local storage to keep user logged in between page refreshes
                  localStorage.setItem('currentUser', JSON.stringify(user));
                //  localStorage.setItem();
              }
          });

    }

    logout() {
        // remove user from local storage to log user out
        localStorage.removeItem('currentUser');
    }
}
