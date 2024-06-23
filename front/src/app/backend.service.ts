import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export class QOne {
  public id!: string;
  public topic!: string;
  public status!: string;
  public submitted_time!: Date;
  public finished_time!: Date;
  public local_start_date!: Date;
  public local_end_date!: Date;
}

export class QTwoTopics {
  public topics!: QTwoTopic[];
}

export class QTwoTopic {
  public topic!: string[];
}

export class QOneCreate {
  public topic!: string;
  public local_start_date!: Date | undefined;
  public local_end_date!: Date | undefined;
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

  public createQOne(data: QOneCreate) {
    return this.httpClient.post<QOne>(this.PATH + `/q1`, data);
  }

  public getQTwo(q1id: string, query: string, k: number) {
    return this.httpClient.get<QTwoTopics>(this.PATH + `/q1/${q1id}/q2`, {
      params: {
        query: query,
        k: k
      }
    });
  }
}
