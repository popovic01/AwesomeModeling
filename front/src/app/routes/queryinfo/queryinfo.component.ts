import { Component } from '@angular/core';
import { BackendService, QOne } from '../../backend.service';
import { ActivatedRoute, Router } from '@angular/router';
import { from, switchMap } from 'rxjs';

@Component({
  selector: 'app-queryinfo',
  standalone: true,
  imports: [],
  templateUrl: './queryinfo.component.html',
  styleUrl: './queryinfo.component.css'
})
export class QueryinfoComponent {
  public qone: QOne | undefined = undefined;

  constructor(private backendService: BackendService, private activatedRoute: ActivatedRoute) {
    this.activatedRoute.params
      .pipe(
        switchMap((params) => this.backendService.getQOne(params['id']))
      )
      .subscribe(
        (res) => {
          this.qone = res;
        }
      );
  }

}
