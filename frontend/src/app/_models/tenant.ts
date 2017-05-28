export class Tenant {
    username: string;
    uid: string;
    enabled: boolean;
    vm: number;
    quota: number;
    surname: string;
    firstname: string;
    usedQuota: number;

    constructor (username: string, uid: string,enabled: boolean,vm: number,quota: number,surname: string,firstname: string,usedQuota: number){
        this.username = username;
        this.uid = uid;
        this.enabled = enabled;
        this.vm = vm;
        this.quota = quota;
        this.surname = surname;
        this.firstname = firstname;
        this.usedQuota = usedQuota;

    }
}
