package com.utils.common.backup.restore;



import com.utils.common.backup.IResponListener;

import java.util.List;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/12
 */

public interface IRestore {

    void restore(String packageName, String destDir, IResponListener responListener);

    void restore(List<String> packageNameList, String restoreDir, IResponListener responListener);
}
