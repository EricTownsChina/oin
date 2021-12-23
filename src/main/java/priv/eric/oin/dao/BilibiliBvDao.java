package priv.eric.oin.dao;

/**
 * @author Eric 840017241@qq.com
 * @date 2021/12/23 20:12
 * <p>
 * Desc:
 */
public interface BilibiliBvDao {

    /**
     * 是否存在BV号
     * @param bvCode BV号
     * @return 是否存在 1 存在 0 不存在
     */
    int existBvCode(String bvCode);

}
