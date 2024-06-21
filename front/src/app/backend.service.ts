import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export class QOne {
  private id!: string;
  public topic!: string;
  private status!: string;
  private submitted_time!: Date;
  private finished_time!: Date;
  private local_start_date!: Date;
  private local_end_date!: Date;
}

@Injectable({
  providedIn: 'root'
})
export class BackendService {

  private PATH = "http://localhost:8080"

  constructor(private httpClient: HttpClient) { }

  public getQOnes(): Observable<QOne[]> {
    return this.httpClient.get<QOne[]>(this.PATH + "/q1/");
  }

  public getQOne(id: string): Observable<QOne> {
    return this.httpClient.get<QOne>(this.PATH + `/q1/${id}`);
  }
}
