import java.util.*;

/**
 * model発行イベントのデータ定義
 * @author myy
 *
 */
public class RecordEvent extends EventObject {
	public static final long serialVersionUID = 1L;
	public RecordEvent(Object src) {
		super(src);
	}
}
