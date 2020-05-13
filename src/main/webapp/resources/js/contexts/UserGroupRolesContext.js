import React, { createContext, useEffect, useState } from "react";
import { getUserGroupRoles } from "../apis/users/groups";

let UserGroupRolesContext;
const { Provider } = (UserGroupRolesContext = createContext());

/**
 * Context to provide a list of User Group Roles
 * @param children
 * @returns {*}
 * @constructor
 */
function UserGroupRolesProvider({ children }) {
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    getUserGroupRoles().then(setRoles);
  }, []);

  /**
   * Translate the role from its un-internationalize key
   * @param {string} key the value of the role
   * @returns {*}
   */
  const getRoleFromKey = (key) => {
    const role = roles.find((r) => r.value === key);
    return role ? role.label : "UNKNOWN";
  };

  return <Provider value={{ roles, getRoleFromKey }}>{children}</Provider>;
}

export { UserGroupRolesContext, UserGroupRolesProvider };
