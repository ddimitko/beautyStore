import { useState } from "react";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import { Button } from "primereact/button";

function SelectPaymentMethod({ onNext, onBack }) {
    const [paymentMethod, setPaymentMethod] = useState("");

    const handleChangePayment = (event) => {
        setPaymentMethod(event.target.value);
    };

    return (
        <div>
            <h2>Select a Payment Method</h2>
            <FormControl fullWidth>
                <InputLabel>Payment Method</InputLabel>
                <Select value={paymentMethod} onChange={handleChangePayment}>
                    <MenuItem value="credit_card">Credit/Debit Card</MenuItem>
                    <MenuItem value="cash">Cash</MenuItem>
                </Select>
            </FormControl>

            <div className="flex pt-4 justify-between">
                <Button label="Back" severity="secondary" icon="pi pi-arrow-left" onClick={onBack} />
                <Button label="Next" icon="pi pi-arrow-right" iconPos="right" disabled={!paymentMethod} onClick={() => onNext(paymentMethod)} />
            </div>
        </div>
    );
}

export default SelectPaymentMethod;
