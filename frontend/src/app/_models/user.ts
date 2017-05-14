export class User {
    email: string;
    firstName: string;
    lastName: string;
    companyName: string;
    street: string;
    city: string;
    postal: string;
    countryId: number;
    identNumber: string;
    vatNumber: string;
    phone: string;


    constructor (  email: string, firstName: string, lastName: string, companyName: string,street: string, city: string, postal: string, countryId: number, identNumber: string, vatNumber: string, phone: string){
        this.email = email;
        this.companyName = companyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.city = city;
        this. postal =postal;
        this.countryId = countryId;
        this.identNumber = identNumber;
        this.vatNumber = vatNumber;
        this.phone = phone;
    }
}
