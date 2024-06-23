import { Component } from '@angular/core';
import { BackendService, QOne } from '../../backend.service';
import { ActivatedRoute, Router } from '@angular/router';
import { from, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-queryinfo',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './queryinfo.component.html',
  styleUrl: './queryinfo.component.css'
})
export class QueryinfoComponent {
  public qone: QOne | undefined = undefined;

  public form = new FormGroup({
    query: new FormControl(''),
    k: new FormControl(50)
  });

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

  onSubmit() {
    console.log(this.form.value)
    this.backendService.getQTwo(this.qone!.id, this.form.value.query!, this.form.value.k!).subscribe((res) => {
      console.log(res);
    })

  }

}
