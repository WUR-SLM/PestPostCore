/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestpostcore;

import java.util.EventListener;
/**
 *
 * @author wesse016
 */
interface AddToLogEventClassListener extends EventListener {
  public void handleAddToLogEventClassEvent(AddToLogEventClass evt);
}  

