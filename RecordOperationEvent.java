import java.util.*;

/**
 * view発行イベントのデータ定義
 * @author myy
 *
 */
public class RecordOperationEvent extends EventObject {
	public static final long serialVersionUID = 1L;
	public RecordOperationEvent(Object src) {
		super(src);
	}
}
