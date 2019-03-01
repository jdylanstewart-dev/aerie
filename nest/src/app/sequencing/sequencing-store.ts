/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { ActionReducerMap } from '@ngrx/store';
import * as fromRoot from '../app-store';
import * as fromCommandDictionary from './reducers/command-dictionary.reducer';
import * as fromEditor from './reducers/editor.reducer';

export interface State {
  commandDictionary: fromCommandDictionary.CommandDictionaryState;
  editor: fromEditor.EditorState;
}

export const reducers: ActionReducerMap<State> = {
  commandDictionary: fromCommandDictionary.reducer,
  editor: fromEditor.reducer,
};

export interface SequencingAppState extends fromRoot.AppState {
  sequencing: State;
}