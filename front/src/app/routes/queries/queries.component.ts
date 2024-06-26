import { Component } from '@angular/core';
import { BackendService, QOne, QOneCreate } from '../../backend.service';
import { RouterModule } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { interval, merge, startWith, Subject, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-queries',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, DatePipe],
  templateUrl: './queries.component.html',
  styleUrl: './queries.component.css'
})
export class QueriesComponent {
  public qones: QOne[] | undefined;

  public createForm;

  private triggerRefresh = new Subject<void>();

  constructor(private backendService: BackendService) {

    merge(interval(5000), this.triggerRefresh).pipe(
      startWith(0),
      switchMap(() => this.backendService.getQOnes())
    )
      .subscribe(
        (res) => {
          this.qones = res;
        },
      );


    this.createForm = new FormGroup({
      topic: new FormControl(''),
      start_date: new FormControl(''),
      end_date: new FormControl('')
    });
  }

  onSubmit() {
    let data = this.createForm.value;

    let qonedata = new QOneCreate();
    qonedata.topic = data.topic!;
    if (data.start_date)
      qonedata.local_start_date = new Date(data.start_date);
    if (data.end_date)
      qonedata.local_end_date = new Date(data.end_date);

    this.backendService.createQOne(qonedata).subscribe((res) => {
      this.triggerRefresh.next();
    });

  }

  deleteEntry(id: string, event: Event) {
    event.stopPropagation();
    this.backendService.deleteQOne(id).subscribe((res) => {
      this.triggerRefresh.next();
    });
  }

}
