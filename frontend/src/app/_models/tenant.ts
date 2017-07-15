export class Tenant {

    constructor ( public username: string,
                 public uid: string,
                 public enabled: boolean,
                 public vm: number,
                 public quota: number,
                 public surname: string,
                 public firstname: string,
                 public usedQuota: number){   }

}
