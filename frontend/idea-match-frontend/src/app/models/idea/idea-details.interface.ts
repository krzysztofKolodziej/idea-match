import { IdeaCategory } from './idea-category.enum';
import { IdeaStatus } from './idea-status.enum';

export interface IdeaDetailsDto {
  id: number;
  title: string;
  location: string;
  description: string;
  goal: string | null;
  status: IdeaStatus;
  category: IdeaCategory;
  username: string;
  createdDate: string;
  expectedStartDate: string | null;
}