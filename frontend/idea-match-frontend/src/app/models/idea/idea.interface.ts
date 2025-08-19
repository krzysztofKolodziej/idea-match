import { IdeaCategory } from './idea-category.enum';

export interface IdeaDto {
  id: number;
  title: string;
  location: string;
  category: IdeaCategory;
  username: string;
  createdDate: string;
}